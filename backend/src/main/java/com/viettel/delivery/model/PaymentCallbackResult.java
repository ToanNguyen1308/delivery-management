package com.viettel.delivery.model;

import java.math.BigDecimal;

/**
 * Ket qua sau khi xac thuc du lieu callback tu cong thanh toan.
 */
public record PaymentCallbackResult(boolean validSignature,
                                    boolean success,
                                    String txnRef,
                                    String transactionNo,
                                    String responseCode,
                                    BigDecimal amount,
                                    String bankCode,
                                    String cardType,
                                    String payDate) {
}
