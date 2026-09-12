package com.viettel.delivery.listener;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.TrackingEventType;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.event.OrderCreatedEvent;
import com.viettel.delivery.event.OrderStatusChangedEvent;
import com.viettel.delivery.repository.OrderRepository;
import com.viettel.delivery.service.CodSettlementService;
import com.viettel.delivery.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ghi lai hanh trinh don hang va tao khoan doi soat COD.
 * Chay dong bo trong cung transaction nghiep vu de du lieu luon nhat quan.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTrackingListener {

    private final OrderRepository orderRepository;
    private final TrackingService trackingService;
    private final CodSettlementService codSettlementService;

    @EventListener
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        findOrder(event.orderId()).ifPresent(order ->
                trackingService.recordEvent(order, TrackingEventType.ORDER_PLACED,
                        "Đơn hàng %s đã được tạo".formatted(order.getOrderCode()),
                        order.getPickupLatitude(), order.getPickupLongitude()));
    }

    @EventListener
    @Transactional
    public void onStatusChanged(OrderStatusChangedEvent event) {
        findOrder(event.orderId()).ifPresent(order -> {
            TrackingEventType eventType = mapEventType(event.toStatus());
            if (eventType != null) {
                trackingService.recordEvent(order, eventType,
                        buildDescription(event), event.latitude(), event.longitude());
            }
            if (OrderStatus.DELIVERED.equals(event.toStatus())) {
                codSettlementService.createOnDelivered(order);
            }
        });
    }

    private String buildDescription(OrderStatusChangedEvent event) {
        String base = event.toStatus().getDescription();
        return event.note() == null || event.note().isBlank() ? base : "%s - %s".formatted(base, event.note());
    }

    private TrackingEventType mapEventType(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> TrackingEventType.ORDER_CONFIRMED;
            case ASSIGNED -> TrackingEventType.SHIPPER_ASSIGNED;
            case PICKED_UP -> TrackingEventType.PICKED_UP;
            case IN_TRANSIT -> TrackingEventType.IN_TRANSIT;
            case DELIVERED -> TrackingEventType.DELIVERED;
            case FAILED -> TrackingEventType.DELIVERY_FAILED;
            case RETURNED -> TrackingEventType.RETURNED;
            case CANCELLED -> TrackingEventType.CANCELLED;
            case CREATED -> TrackingEventType.ORDER_PLACED;
        };
    }

    private java.util.Optional<Order> findOrder(Long orderId) {
        java.util.Optional<Order> order = orderRepository.findByIdAndIsDeletedFalse(orderId);
        if (order.isEmpty()) {
            log.warn("Bo qua ghi tracking vi khong tim thay don hang {}", orderId);
        }
        return order;
    }
}
