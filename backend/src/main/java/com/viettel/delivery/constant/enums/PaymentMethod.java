package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod implements BaseEnum {

    COD("Thanh toán khi nhận hàng"),
    VNPAY("Thanh toán online qua VNPay");

    private final String description;

    public boolean isOnline() {
        return this == VNPAY;
    }
}
