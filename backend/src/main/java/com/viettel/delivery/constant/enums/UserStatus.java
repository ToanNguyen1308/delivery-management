package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus implements BaseEnum {

    ACTIVE("Đang hoạt động"),
    INACTIVE("Ngừng hoạt động"),
    LOCKED("Bị khóa");

    private final String description;
}
