package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType implements BaseEnum {

    ORDER_CREATED("Đơn hàng mới"),
    ORDER_STATUS_CHANGED("Đơn hàng đổi trạng thái"),
    ORDER_ASSIGNED("Đơn hàng được phân công"),
    ORDER_ACCEPTED("Shipper đã nhận đơn"),
    ORDER_REJECTED("Shipper từ chối đơn"),
    ORDER_DELIVERED("Giao hàng thành công"),
    ORDER_CANCELLED("Đơn hàng bị hủy"),
    ORDER_OVERDUE("Đơn hàng quá hạn giao"),
    PAYMENT_SUCCESS("Thanh toán thành công"),
    PAYMENT_FAILED("Thanh toán thất bại"),
    COD_SETTLED("Đã đối soát COD"),
    SYSTEM("Thông báo hệ thống");

    private final String description;
}
