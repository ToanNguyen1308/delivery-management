package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DispatchStrategyType implements BaseEnum {

    NEAREST("Ưu tiên shipper gần điểm lấy hàng nhất"),
    LEAST_LOAD("Ưu tiên shipper đang ít đơn nhất"),
    ROUND_ROBIN("Chia đơn luân phiên");

    private final String description;
}
