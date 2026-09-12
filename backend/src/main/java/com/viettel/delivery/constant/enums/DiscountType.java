package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType implements BaseEnum {

    PERCENT("Giảm theo phần trăm"),
    FIXED("Giảm số tiền cố định"),
    FREE_SHIP("Miễn phí vận chuyển");

    private final String description;
}
