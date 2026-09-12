package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrackingEventType implements BaseEnum {

    ORDER_PLACED("Đơn hàng được tạo"),
    ORDER_CONFIRMED("Đơn hàng đã được xác nhận"),
    SHIPPER_ASSIGNED("Đã phân công shipper"),
    PICKED_UP("Shipper đã lấy hàng"),
    IN_TRANSIT("Hàng đang trên đường giao"),
    LOCATION_UPDATE("Cập nhật vị trí"),
    DELIVERED("Giao hàng thành công"),
    DELIVERY_FAILED("Giao hàng không thành công"),
    RETURNED("Hàng đã hoàn trả"),
    CANCELLED("Đơn hàng bị hủy");

    private final String description;
}
