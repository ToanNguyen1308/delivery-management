package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssignType implements BaseEnum {

    MANUAL("Điều phối viên gán thủ công"),
    AUTO("Hệ thống gán tự động");

    private final String description;
}
