package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType implements BaseEnum {

    STANDARD("Giao tiêu chuẩn (2-3 ngày)", 48),
    EXPRESS("Giao nhanh (24h)", 24),
    SAME_DAY("Giao trong ngày (6h)", 6);

    private final String description;
    private final int slaHours;
}
