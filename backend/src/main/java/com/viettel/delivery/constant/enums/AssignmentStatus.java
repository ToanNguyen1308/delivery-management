package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssignmentStatus implements BaseEnum {

    PENDING("Chờ shipper xác nhận"),
    ACCEPTED("Shipper đã nhận"),
    REJECTED("Shipper từ chối"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy phân công");

    private final String description;
}
