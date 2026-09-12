package com.viettel.delivery.service.impl;

import com.viettel.delivery.config.properties.DispatchProperties;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.AssignType;
import com.viettel.delivery.constant.enums.AssignmentStatus;
import com.viettel.delivery.constant.enums.DispatchStrategyType;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.dto.request.AssignOrderRequest;
import com.viettel.delivery.dto.request.AssignmentResponseRequest;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.AssignmentResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.entity.DeliveryAssignment;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.event.OrderAssignedEvent;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.AssignmentMapper;
import com.viettel.delivery.repository.DeliveryAssignmentRepository;
import com.viettel.delivery.repository.OrderRepository;
import com.viettel.delivery.repository.ShipperRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.DispatchService;
import com.viettel.delivery.service.OrderStatusService;
import com.viettel.delivery.service.ShipperService;
import com.viettel.delivery.service.dispatch.ShipperAssignmentStrategy;
import com.viettel.delivery.util.GeoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DispatchServiceImpl implements DispatchService {

    private final Map<DispatchStrategyType, ShipperAssignmentStrategy> strategies =
            new EnumMap<>(DispatchStrategyType.class);

    private final OrderRepository orderRepository;
    private final ShipperRepository shipperRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final AssignmentMapper assignmentMapper;
    private final OrderStatusService orderStatusService;
    private final ShipperService shipperService;
    private final DispatchProperties dispatchProperties;
    private final ApplicationEventPublisher eventPublisher;

    public DispatchServiceImpl(List<ShipperAssignmentStrategy> strategyList,
                               OrderRepository orderRepository,
                               ShipperRepository shipperRepository,
                               DeliveryAssignmentRepository assignmentRepository,
                               AssignmentMapper assignmentMapper,
                               OrderStatusService orderStatusService,
                               ShipperService shipperService,
                               DispatchProperties dispatchProperties,
                               ApplicationEventPublisher eventPublisher) {
        strategyList.forEach(strategy -> strategies.put(strategy.getType(), strategy));
        this.orderRepository = orderRepository;
        this.shipperRepository = shipperRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentMapper = assignmentMapper;
        this.orderStatusService = orderStatusService;
        this.shipperService = shipperService;
        this.dispatchProperties = dispatchProperties;
        this.eventPublisher = eventPublisher;
        log.info("Dieu phoi su dung chien luoc mac dinh: {}", dispatchProperties.strategy());
    }

    @Override
    @Transactional
    public AssignmentResponse assign(AssignOrderRequest request) {
        Order order = orderRepository.findByIdWithDetails(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getCurrentShipper() != null) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_ASSIGNED);
        }
        if (!OrderStatus.CONFIRMED.equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_TRANSITION,
                    order.getStatus().getDescription(), OrderStatus.ASSIGNED.getDescription());
        }

        boolean autoAssigned = request.getShipperId() == null;
        Shipper shipper = autoAssigned ? selectShipperAutomatically(order) : loadShipper(request.getShipperId());
        validateShipperCapacity(shipper);

        DeliveryAssignment assignment = assignmentRepository.save(DeliveryAssignment.builder()
                .order(order)
                .shipper(shipper)
                .assignedByUserId(SecurityUtil.getCurrentUserIdOptional().orElse(null))
                .status(AssignmentStatus.PENDING)
                .assignType(autoAssigned ? AssignType.AUTO : AssignType.MANUAL)
                .distanceKm(calculateDistance(order, shipper))
                .assignedAt(LocalDateTime.now())
                .note(request.getNote())
                .build());

        order.setCurrentShipper(shipper);
        shipper.increaseLoad();
        orderStatusService.changeStatus(order, OrderStatus.ASSIGNED,
                "Phân công cho shipper %s".formatted(shipper.getShipperCode()));

        eventPublisher.publishEvent(new OrderAssignedEvent(order.getId(), order.getOrderCode(),
                shipper.getId(), shipper.getUser().getId(), order.getCustomer().getId(), autoAssigned));

        log.info("Da gan don {} cho shipper {} ({})", order.getOrderCode(), shipper.getShipperCode(),
                autoAssigned ? "tu dong" : "thu cong");
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse respond(Long assignmentId, AssignmentResponseRequest request) {
        DeliveryAssignment assignment = assignmentRepository.findByIdWithDetails(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ASSIGNMENT_NOT_FOUND));

        Shipper currentShipper = shipperService.getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        if (!currentShipper.getId().equals(assignment.getShipper().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
        if (!AssignmentStatus.PENDING.equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_PENDING);
        }

        assignment.setRespondedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(request.getAccepted())) {
            assignment.setStatus(AssignmentStatus.ACCEPTED);
            log.info("Shipper {} da nhan don {}", currentShipper.getShipperCode(),
                    assignment.getOrder().getOrderCode());
        } else {
            rejectAssignment(assignment, currentShipper, request.getRejectReason());
        }
        return assignmentMapper.toResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AssignmentResponse> getMyAssignments(BaseSearchRequest request,
                                                             List<AssignmentStatus> statuses) {
        Shipper shipper = shipperService.getShipperEntityByUserId(SecurityUtil.getCurrentUserId());
        List<AssignmentStatus> filter = CollectionUtils.isEmpty(statuses)
                ? List.copyOf(EnumSet.allOf(AssignmentStatus.class))
                : statuses;
        Page<DeliveryAssignment> page =
                assignmentRepository.findByShipper(shipper.getId(), filter, request.toUnsortedPageable());
        return PageResponse.of(page, assignmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByOrder(Long orderId) {
        return assignmentMapper.toResponseList(assignmentRepository.findByOrderId(orderId));
    }

    /**
     * Khi shipper tu choi, don duoc tra ve hang cho de dieu phoi vien gan lai.
     */
    private void rejectAssignment(DeliveryAssignment assignment, Shipper shipper, String reason) {
        assignment.setStatus(AssignmentStatus.REJECTED);
        assignment.setRejectReason(reason);

        Order order = assignment.getOrder();
        order.setCurrentShipper(null);
        shipper.decreaseLoad();
        orderStatusService.changeStatus(order, OrderStatus.CONFIRMED,
                "Shipper %s từ chối đơn: %s".formatted(shipper.getShipperCode(),
                        reason == null ? "không có lý do" : reason));

        log.info("Shipper {} tu choi don {}, don quay lai hang cho dieu phoi",
                shipper.getShipperCode(), order.getOrderCode());
    }

    private Shipper selectShipperAutomatically(Order order) {
        List<Shipper> candidates = shipperRepository
                .findAvailableShippers(EnumSet.of(ShipperStatus.ONLINE, ShipperStatus.BUSY));
        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NO_SHIPPER_AVAILABLE);
        }

        ShipperAssignmentStrategy strategy = strategies.get(dispatchProperties.strategy());
        return strategy.selectShipper(order, candidates)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NO_SHIPPER_AVAILABLE));
    }

    private Shipper loadShipper(Long shipperId) {
        return shipperRepository.findByIdWithUser(shipperId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHIPPER_NOT_FOUND));
    }

    private void validateShipperCapacity(Shipper shipper) {
        if (!shipper.getStatus().canReceiveOrder()) {
            throw new BusinessException(ErrorCode.SHIPPER_NOT_AVAILABLE);
        }
        if (!shipper.hasCapacity()) {
            throw new BusinessException(ErrorCode.SHIPPER_OVERLOADED);
        }
    }

    private BigDecimal calculateDistance(Order order, Shipper shipper) {
        if (!shipper.hasLocation() || order.getPickupLatitude() == null) {
            return null;
        }
        return GeoUtil.distanceKm(shipper.getCurrentLatitude(), shipper.getCurrentLongitude(),
                order.getPickupLatitude(), order.getPickupLongitude());
    }
}
