package com.viettel.delivery.scheduler;

import com.viettel.delivery.constant.enums.NotificationType;
import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.entity.Order;
import com.viettel.delivery.repository.OrderRepository;
import com.viettel.delivery.repository.RefreshTokenRepository;
import com.viettel.delivery.service.NotificationService;
import com.viettel.delivery.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cac tac vu chay dinh ky: nhac don qua han va don dep token het han.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduledTasks {

    private static final String REFERENCE_TYPE_ORDER = "ORDER";

    private final OrderRepository orderRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationService notificationService;
    private final MessageUtil messageUtil;

    /**
     * Quet moi 30 phut, canh bao cac don da qua han giao du kien nhung chua hoan tat.
     */
    @Scheduled(fixedDelayString = "PT30M", initialDelayString = "PT2M")
    @Transactional(readOnly = true)
    public void notifyOverdueOrders() {
        List<Order> overdueOrders = orderRepository.findOverdueOrders(LocalDateTime.now(),
                List.copyOf(OrderStatus.activeStatuses()));
        if (overdueOrders.isEmpty()) {
            return;
        }

        overdueOrders.forEach(order -> notificationService.notifyUser(order.getCustomer().getId(),
                NotificationType.ORDER_OVERDUE,
                messageUtil.get("notify.order.overdue.title"),
                messageUtil.get("notify.order.overdue.content", order.getOrderCode()),
                REFERENCE_TYPE_ORDER, order.getId(), order.getOrderCode()));

        log.info("Da gui canh bao cho {} don hang qua han giao", overdueOrders.size());
    }

    /**
     * Xoa refresh token da het han de bang du lieu khong phinh to.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Da xoa {} refresh token het han", deleted);
        }
    }
}
