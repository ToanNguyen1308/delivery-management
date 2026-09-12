package com.viettel.delivery.event;

/**
 * Phat khi don hang duoc tao thanh cong, dung de gui thong bao bat dong bo.
 */
public record OrderCreatedEvent(Long orderId, String orderCode, Long customerId) {
}
