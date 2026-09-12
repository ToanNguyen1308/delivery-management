package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodSettlementStatus implements BaseEnum {

    HOLDING("Shipper đang giữ tiền"),
    SUBMITTED("Shipper đã nộp tiền, chờ xác nhận"),
    CONFIRMED("Kế toán đã xác nhận");

    private final String description;
}
