package com.viettel.delivery.model;

import com.viettel.delivery.entity.Voucher;

import java.math.BigDecimal;

/**
 * Ket qua kiem tra ma giam gia. Khi khong hop le van tra ve ly do de frontend hien thi,
 * thay vi nem exception lam hong luong tinh cuoc.
 */
public record VoucherDiscountResult(boolean applied,
                                    BigDecimal discountAmount,
                                    Voucher voucher,
                                    String messageCode) {

    public static VoucherDiscountResult none() {
        return new VoucherDiscountResult(false, BigDecimal.ZERO, null, null);
    }

    public static VoucherDiscountResult rejected(String messageCode) {
        return new VoucherDiscountResult(false, BigDecimal.ZERO, null, messageCode);
    }

    public static VoucherDiscountResult applied(BigDecimal discountAmount, Voucher voucher) {
        return new VoucherDiscountResult(true, discountAmount, voucher, null);
    }
}
