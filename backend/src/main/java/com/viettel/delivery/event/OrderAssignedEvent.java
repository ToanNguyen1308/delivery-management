package com.viettel.delivery.event;

/**
 * Phat khi dieu phoi vien (hoac thuat toan tu dong) gan don cho shipper.
 */
public record OrderAssignedEvent(Long orderId,
                                 String orderCode,
                                 Long shipperId,
                                 Long shipperUserId,
                                 Long customerId,
                                 boolean autoAssigned) {
}
