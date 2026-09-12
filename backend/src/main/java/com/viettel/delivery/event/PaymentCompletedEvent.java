package com.viettel.delivery.event;

import java.math.BigDecimal;

/**
 * Phat sau khi cong thanh toan xac nhan ket qua giao dich.
 */
public record PaymentCompletedEvent(Long orderId,
                                    String orderCode,
                                    Long customerId,
                                    BigDecimal amount,
                                    boolean success) {
}
