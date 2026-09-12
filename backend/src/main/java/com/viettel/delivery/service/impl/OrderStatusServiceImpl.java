package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.CodSettlementStatus;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.RoleGroupCode;
import com.viettel.delivery.dto.request.OrderStatusUpdateRequest;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.OrderStatusHistory;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.event.OrderStatusChangedEvent;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.repository.OrderStatusHistoryRepository;
import com.viettel.delivery.security.CustomUserDetails;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void changeStatus(Order order, OrderStatusUpdateRequest request) {
        OrderStatus current = order.getStatus();
        OrderStatus target = request.getStatus();
        validateTransition(current, target);
        validateActor(order, target);
        validateRequiredData(order, request, target);

        applyStatusSideEffects(order, target, request);
        order.setStatus(target);

        saveHistory(order, current, target, request.getNote());
        publishEvent(order, current, target, request.getNote(),
                request.getLatitude(), request.getLongitude());

        log.info("Don hang {} chuyen tu {} sang {}", order.getOrderCode(), current, target);
    }

    @Override
    @Transactional
    public void changeStatus(Order order, OrderStatus targetStatus, String note) {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setStatus(targetStatus);
        request.setNote(note);
        changeStatus(order, request);
    }

    @Override
    public void validateTransition(OrderStatus current, OrderStatus target) {
        if (current == target) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_TRANSITION,
                    current.getDescription(), target.getDescription());
        }
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_TRANSITION,
                    current.getDescription(), target.getDescription());
        }
    }

    private void validateActor(Order order, OrderStatus target) {
        if (!target.isShipperOperation()) {
            return;
        }
        CustomUserDetails user = SecurityUtil.requireCurrentUser();
        if (!user.hasRoleGroup(RoleGroupCode.SHIPPER.name())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_SHIPPER_ONLY, HttpStatus.FORBIDDEN);
        }
        Shipper shipper = order.getCurrentShipper();
        if (shipper == null || shipper.getUser() == null || !user.getUserId().equals(shipper.getUser().getId())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_SHIPPER_ONLY, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Anh xac nhan giao hang la bat buoc truoc khi dong don thanh cong.
     */
    private void validateRequiredData(Order order, OrderStatusUpdateRequest request, OrderStatus target) {
        if (OrderStatus.DELIVERED.equals(target)
                && !StringUtils.hasText(request.getProofImageUrl())
                && !StringUtils.hasText(order.getProofImageUrl())) {
            throw new BusinessException(ErrorCode.ORDER_PROOF_REQUIRED);
        }
    }

    private void applyStatusSideEffects(Order order, OrderStatus target, OrderStatusUpdateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Shipper shipper = order.getCurrentShipper();

        switch (target) {
            case PICKED_UP -> order.setPickedUpAt(now);
            case DELIVERED -> {
                order.setDeliveredAt(now);
                if (StringUtils.hasText(request.getProofImageUrl())) {
                    order.setProofImageUrl(request.getProofImageUrl());
                }
                if (order.isCodOrder()) {
                    order.setCodSettlementStatus(CodSettlementStatus.HOLDING);
                }
                if (shipper != null) {
                    shipper.setTotalDelivered(shipper.getTotalDelivered() + 1);
                    shipper.decreaseLoad();
                }
            }
            case FAILED -> {
                order.setFailureReason(request.getFailureReason());
                if (shipper != null) {
                    shipper.setTotalFailed(shipper.getTotalFailed() + 1);
                }
            }
            case RETURNED -> {
                if (shipper != null) {
                    shipper.decreaseLoad();
                }
            }
            case CANCELLED -> {
                order.setCancelReason(StringUtils.hasText(request.getNote())
                        ? request.getNote()
                        : order.getCancelReason());
                if (shipper != null) {
                    shipper.decreaseLoad();
                    order.setCurrentShipper(null);
                }
            }
            default -> {
                // CONFIRMED, ASSIGNED, IN_TRANSIT khong co xu ly bo sung tai day
            }
        }
    }

    private void saveHistory(Order order, OrderStatus from, OrderStatus to, String note) {
        Optional<CustomUserDetails> currentUser = SecurityUtil.getCurrentUser();
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(from)
                .toStatus(to)
                .note(note)
                .changedByUserId(currentUser.map(CustomUserDetails::getUserId).orElse(null))
                .changedByName(currentUser.map(CustomUserDetails::getFullName).orElse("Hệ thống"))
                .changedAt(LocalDateTime.now())
                .build());
    }

    private void publishEvent(Order order, OrderStatus from, OrderStatus to, String note,
                              java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        Long shipperUserId = order.getCurrentShipper() != null && order.getCurrentShipper().getUser() != null
                ? order.getCurrentShipper().getUser().getId()
                : null;
        eventPublisher.publishEvent(new OrderStatusChangedEvent(
                order.getId(), order.getOrderCode(), order.getCustomer().getId(),
                shipperUserId, from, to, note, latitude, longitude));
    }
}
