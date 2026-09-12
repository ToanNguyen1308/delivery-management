package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum VehicleType implements BaseEnum {

    MOTORBIKE("Xe máy", new BigDecimal("30")),
    CAR("Ô tô", new BigDecimal("200")),
    VAN("Xe tải van", new BigDecimal("800")),
    TRUCK("Xe tải", new BigDecimal("3000"));

    private final String description;
    private final BigDecimal defaultCapacityKg;
}
