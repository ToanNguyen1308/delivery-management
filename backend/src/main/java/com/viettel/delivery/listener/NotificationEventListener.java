package com.viettel.delivery.listener;

import com.viettel.delivery.constant.enums.NotificationType;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.event.OrderAssignedEvent;
import com.viettel.delivery.event.OrderCreatedEvent;
import com.viettel.delivery.event.OrderStatusChangedEvent;
import com.viettel.delivery.event.PaymentCompletedEvent;
import com.viettel.delivery.util.MessageUtil;
import com.viettel.delivery.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Gui thong bao bat dong bo sau khi transaction nghiep vu commit thanh cong,
 * de loi gui thong bao khong lam rollback nghiep vu chinh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final String REFERENCE_TYPE_ORDER = "ORDER";

    private final NotificationService notificationService;
    private final MessageUtil messageUtil;

    @Async
    @TransactionalEventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        notificationService.notifyUser(event.customerId(), NotificationType.ORDER_CREATED,
                messageUtil.get("notify.order.created.title"),
                messageUtil.get("notify.order.created.content", event.orderCode()),
                REFERENCE_TYPE_ORDER, event.orderId(), event.orderCode());
    }

    @Async
    @TransactionalEventListener
    public void onOrderAssigned(OrderAssignedEvent event) {
        notificationService.notifyUser(event.shipperUserId(), NotificationType.ORDER_ASSIGNED,
                messageUtil.get("notify.order.assigned.title"),
                messageUtil.get("notify.order.assigned.content", event.orderCode()),
                REFERENCE_TYPE_ORDER, event.orderId(), event.orderCode());
    }

    @Async
    @TransactionalEventListener
    public void onStatusChanged(OrderStatusChangedEvent event) {
        NotificationType type = resolveType(event.toStatus());
        String title = messageUtil.get(resolveTitleKey(event.toStatus()));
        String content = resolveContent(event);

        notificationService.notifyUser(event.customerId(), type, title, content,
                REFERENCE_TYPE_ORDER, event.orderId(), event.orderCode());

        // Shipper cũng cần biết khi đơn bị hủy
        if (event.shipperUserId() != null && OrderStatus.CANCELLED.equals(event.toStatus())) {
            notificationService.notifyUser(event.shipperUserId(), type, title, content,
                    REFERENCE_TYPE_ORDER, event.orderId(), event.orderCode());
        }
    }

    @Async
    @TransactionalEventListener
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event.success()) {
            notificationService.notifyUser(event.customerId(), NotificationType.PAYMENT_SUCCESS,
                    messageUtil.get("notify.payment.success.title"),
                    messageUtil.get("notify.payment.success.content", event.orderCode(), formatMoney(event)),
                    REFERENCE_TYPE_ORDER, event.orderId(), event.orderCode());
            return;
        }
        notificationService.notifyUser(event.customerId(), NotificationType.PAYMENT_FAILED,
                messageUtil.get("notify.payment.failed.title"),
                messageUtil.get("notify.payment.failed.content", event.orderCode()),
                REFERENCE_TYPE_ORDER, event.orderId(), event.orderCode());
    }

    private NotificationType resolveType(OrderStatus status) {
        return switch (status) {
            case DELIVERED -> NotificationType.ORDER_DELIVERED;
            case CANCELLED -> NotificationType.ORDER_CANCELLED;
            case ASSIGNED -> NotificationType.ORDER_ASSIGNED;
            default -> NotificationType.ORDER_STATUS_CHANGED;
        };
    }

    private String resolveTitleKey(OrderStatus status) {
        return switch (status) {
            case DELIVERED -> "notify.order.delivered.title";
            case CANCELLED -> "notify.order.cancelled.title";
            default -> "notify.order.statusChanged.title";
        };
    }

    private String resolveContent(OrderStatusChangedEvent event) {
        return switch (event.toStatus()) {
            case DELIVERED -> messageUtil.get("notify.order.delivered.content", event.orderCode());
            case CANCELLED -> messageUtil.get("notify.order.cancelled.content", event.orderCode());
            default -> messageUtil.get("notify.order.statusChanged.content",
                    event.orderCode(), event.toStatus().getDescription());
        };
    }

    private String formatMoney(PaymentCompletedEvent event) {
        return NumberFormat.getInstance(Locale.of("vi", "VN")).format(event.amount());
    }
}
