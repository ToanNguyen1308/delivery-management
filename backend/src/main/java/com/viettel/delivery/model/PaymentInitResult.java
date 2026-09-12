package com.viettel.delivery.model;

/**
 * Ket qua khoi tao giao dich: URL de chuyen huong nguoi dung sang cong thanh toan.
 */
public record PaymentInitResult(String payUrl, String rawRequest) {
}
