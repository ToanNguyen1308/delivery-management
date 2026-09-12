package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoucherStatus implements BaseEnum {

    ACTIVE("Đang hoạt động"),
    INACTIVE("Ngừng hoạt động"),
    EXPIRED("Hết hạn");

    private final String description;
}
