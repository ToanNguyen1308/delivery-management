package com.viettel.delivery.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Ma nhom quyen mac dinh cua he thong. Danh sach nhom quyen luu trong bang role_groups,
 * enum nay chi dung de tham chieu trong code (tranh hard-code chuoi).
 */
@Getter
@RequiredArgsConstructor
public enum RoleGroupCode implements BaseEnum {

    ADMIN("Quản trị hệ thống"),
    DISPATCHER("Điều phối viên"),
    SHIPPER("Nhân viên giao hàng"),
    CUSTOMER("Khách hàng");

    private final String description;
}
