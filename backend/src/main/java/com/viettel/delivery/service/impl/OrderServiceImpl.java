package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.RoleGroupCode;
import com.viettel.delivery.dto.request.OrderCreateRequest;
import com.viettel.delivery.dto.request.OrderItemRequest;
import com.viettel.delivery.dto.request.OrderStatusUpdateRequest;
import com.viettel.delivery.dto.request.OrderUpdateRequest;
import com.viettel.delivery.dto.response.EnumResponse;
import com.viettel.delivery.dto.response.OrderResponse;
import com.viettel.delivery.dto.response.OrderStatusHistoryResponse;
import com.viettel.delivery.dto.response.OrderSummaryResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.search.OrderSearchRequest;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.entity.OrderItem;
import com.viettel.delivery.entity.OrderStatusHistory;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.entity.User;
import com.viettel.delivery.event.OrderCreatedEvent;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.OrderMapper;
import com.viettel.delivery.model.FeeBreakdown;
import com.viettel.delivery.model.FeeCalculationContext;
import com.viettel.delivery.model.VoucherDiscountResult;
import com.viettel.delivery.repository.OrderRepository;
import com.viettel.delivery.repository.OrderStatusHistoryRepository;
import com.viettel.delivery.repository.ShipperRepository;
import com.viettel.delivery.repository.UserRepository;
import com.viettel.delivery.repository.specification.OrderSpecification;
import com.viettel.delivery.security.CustomUserDetails;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.OrderService;
import com.viettel.delivery.service.OrderStatusService;
import com.viettel.delivery.service.VoucherService;
import com.viettel.delivery.service.pricing.ShippingFeeCalculator;
import com.viettel.delivery.util.CodeGenerator;
import com.viettel.delivery.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final UserRepository userRepository;
    private final ShipperRepository shipperRepository;
    private final OrderMapper orderMapper;
    private final ShippingFeeCalculator shippingFeeCalculator;
    private final VoucherService voucherService;
    private final OrderStatusService orderStatusService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> search(OrderSearchRequest request) {
        DataScope scope = resolveDataScope();
        Page<Order> page = orderRepository.findAll(
                OrderSpecification.build(request, scope.customerId(), scope.shipperId()),
                request.toPageable());
        return PageResponse.of(page, orderMapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return toDetailResponse(getAccessibleOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getByOrderCode(String orderCode) {
        Order order = orderRepository.findByOrderCodeWithDetails(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        verifyAccess(order);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        return createForCustomer(request, SecurityUtil.getCurrentUserId());
    }

    @Override
    @Transactional
    public OrderResponse createForCustomer(OrderCreateRequest request, Long customerId) {
        User customer = userRepository.findByIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        BigDecimal distanceKm = GeoUtil.roadDistanceKm(request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDeliveryLatitude(), request.getDeliveryLongitude());

        FeeBreakdown breakdown = shippingFeeCalculator.calculate(request.getServiceType(),
                new FeeCalculationContext(distanceKm,
                        request.getWeightKg(),
                        request.getCodAmount(),
                        Boolean.TRUE.equals(request.getFragile()),
                        Boolean.TRUE.equals(request.getRemoteArea()),
                        LocalDateTime.now()));

        VoucherDiscountResult voucherResult =
                voucherService.evaluate(request.getVoucherCode(), breakdown.shippingFee(), customerId);

        Order order = buildOrder(request, customer, distanceKm, breakdown, voucherResult);
        attachItems(order, request.getItems());

        Order saved = orderRepository.save(order);
        voucherService.consume(voucherResult, customerId, saved.getId());
        saveInitialHistory(saved, customer);

        eventPublisher.publishEvent(new OrderCreatedEvent(saved.getId(), saved.getOrderCode(), customer.getId()));
        log.info("Da tao don hang {} cua khach hang {}", saved.getOrderCode(), customer.getUsername());
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, OrderUpdateRequest request) {
        Order order = getAccessibleOrder(id);
        if (!order.isEditable()) {
            throw new BusinessException(ErrorCode.ORDER_NOT_EDITABLE);
        }
        orderMapper.updateEntity(order, request);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse confirm(Long id) {
        Order order = getAccessibleOrder(id);
        orderStatusService.changeStatus(order, OrderStatus.CONFIRMED, "Điều phối viên xác nhận đơn hàng");
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = getAccessibleOrder(id);
        orderStatusService.changeStatus(order, request);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public void cancel(Long id, String reason) {
        Order order = getAccessibleOrder(id);
        order.setCancelReason(reason);
        orderStatusService.changeStatus(order, OrderStatus.CANCELLED, reason);
        voucherService.release(order.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getStatusHistory(Long id) {
        getAccessibleOrder(id);
        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderId(id);
        return orderMapper.toHistoryResponseList(histories);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getAccessibleOrder(Long id) {
        Order order = orderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        verifyAccess(order);
        return order;
    }

    private Order buildOrder(OrderCreateRequest request,
                             User customer,
                             BigDecimal distanceKm,
                             FeeBreakdown breakdown,
                             VoucherDiscountResult voucherResult) {
        BigDecimal totalAmount = breakdown.shippingFee()
                .subtract(voucherResult.discountAmount())
                .max(BigDecimal.ZERO);

        return Order.builder()
                .orderCode(generateOrderCode())
                .customer(customer)
                .senderName(request.getSenderName())
                .senderPhone(request.getSenderPhone())
                .pickupAddress(request.getPickupAddress())
                .pickupDistrict(request.getPickupDistrict())
                .pickupProvince(request.getPickupProvince())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryDistrict(request.getDeliveryDistrict())
                .deliveryProvince(request.getDeliveryProvince())
                .deliveryLatitude(request.getDeliveryLatitude())
                .deliveryLongitude(request.getDeliveryLongitude())
                .packageDescription(request.getPackageDescription())
                .weightKg(request.getWeightKg())
                .lengthCm(request.getLengthCm())
                .widthCm(request.getWidthCm())
                .heightCm(request.getHeightCm())
                .fragile(Boolean.TRUE.equals(request.getFragile()))
                .serviceType(request.getServiceType())
                .distanceKm(distanceKm)
                .shippingFee(breakdown.shippingFee())
                .surcharge(breakdown.surcharge())
                .discountAmount(voucherResult.discountAmount())
                .codAmount(request.getCodAmount() == null ? BigDecimal.ZERO : request.getCodAmount())
                .totalAmount(totalAmount)
                .voucher(voucherResult.applied() ? voucherResult.voucher() : null)
                .voucherCode(voucherResult.applied() ? voucherResult.voucher().getCode() : null)
                .status(OrderStatus.CREATED)
                .paymentMethod(request.getPaymentMethod())
                .expectedDeliveryAt(LocalDateTime.now().plusHours(request.getServiceType().getSlaHours()))
                .note(request.getNote())
                .build();
    }

    private void attachItems(Order order, List<OrderItemRequest> itemRequests) {
        if (CollectionUtils.isEmpty(itemRequests)) {
            return;
        }
        itemRequests.stream()
                .map(orderMapper::toItemEntity)
                .forEach(order::addItem);
    }

    private void saveInitialHistory(Order order, User customer) {
        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .toStatus(OrderStatus.CREATED)
                .note("Khách hàng tạo đơn hàng")
                .changedByUserId(customer.getId())
                .changedByName(customer.getFullName())
                .changedAt(LocalDateTime.now())
                .build());
    }

    private OrderResponse toDetailResponse(Order order) {
        OrderResponse response = orderMapper.toResponse(order);
        response.setItems(order.getItems().stream()
                .filter(item -> Boolean.FALSE.equals(item.getIsDeleted()))
                .map(orderMapper::toItemResponse)
                .toList());
        boolean assignedShipper = isCurrentUserAssignedShipper(order);
        response.setNextStatuses(order.getStatus().nextStatuses().stream()
                .filter(status -> !status.isShipperOperation() || assignedShipper)
                .map(EnumResponse::of)
                .toList());
        return response;
    }

    private boolean isCurrentUserAssignedShipper(Order order) {
        return SecurityUtil.getCurrentUser()
                .filter(user -> user.hasRoleGroup(RoleGroupCode.SHIPPER.name()))
                .map(user -> order.getCurrentShipper() != null
                        && order.getCurrentShipper().getUser() != null
                        && user.getUserId().equals(order.getCurrentShipper().getUser().getId()))
                .orElse(false);
    }

    /**
     * Khach hang chi duoc xem don cua minh, shipper chi duoc xem don duoc phan cong.
     */
    private void verifyAccess(Order order) {
        DataScope scope = resolveDataScope();
        if (scope.customerId() != null && !scope.customerId().equals(order.getCustomer().getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
        if (scope.shipperId() != null
                && (order.getCurrentShipper() == null
                || !scope.shipperId().equals(order.getCurrentShipper().getId()))) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        }
    }

    private DataScope resolveDataScope() {
        Optional<CustomUserDetails> currentUser = SecurityUtil.getCurrentUser();
        if (currentUser.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (SecurityUtil.canViewAllData()) {
            return new DataScope(null, null);
        }
        if (SecurityUtil.hasAuthority(PermissionCode.SHIPPER_SELF)) {
            Long shipperId = shipperRepository.findByUserIdWithUser(currentUser.get().getUserId())
                    .map(Shipper::getId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPER_PROFILE_MISSING));
            return new DataScope(null, shipperId);
        }
        return new DataScope(currentUser.get().getUserId(), null);
    }

    private String generateOrderCode() {
        String code = CodeGenerator.orderCode();
        while (orderRepository.existsByOrderCode(code)) {
            code = CodeGenerator.orderCode();
        }
        return code;
    }

    /**
     * Gioi han pham vi du lieu duoc phep truy cap cua nguoi dang dang nhap.
     */
    private record DataScope(Long customerId, Long shipperId) {
    }
}
