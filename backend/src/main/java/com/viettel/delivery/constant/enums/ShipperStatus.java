package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipperStatus implements BaseEnum {

    ONLINE("Sẵn sàng nhận đơn"),
    BUSY("Đang giao hàng"),
    OFFLINE("Ngoại tuyến"),
    SUSPENDED("Tạm khóa");

    private final String description;

    public boolean canReceiveOrder() {
        return this == ONLINE || this == BUSY;
    }
}
