package com.viettel.delivery.constant;

/**
 * function_code cho @PreAuthorize. Phải trùng bảng functions (Liquibase 002-seed-rbac).
 */
public final class PermissionCode {

    private PermissionCode() {
    }

    /* ---------- Người dùng ---------- */
    public static final String USER_VIEW = "ROLE_USER_VIEW";
    public static final String USER_CREATE = "ROLE_USER_CREATE";
    public static final String USER_UPDATE = "ROLE_USER_UPDATE";
    public static final String USER_DELETE = "ROLE_USER_DELETE";

    /* ---------- Nhóm quyền ---------- */
    public static final String ROLE_VIEW = "ROLE_ROLE_VIEW";
    public static final String ROLE_MANAGE = "ROLE_ROLE_MANAGE";

    /* ---------- Shipper ---------- */
    public static final String SHIPPER_VIEW = "ROLE_SHIPPER_VIEW";
    public static final String SHIPPER_CREATE = "ROLE_SHIPPER_CREATE";
    public static final String SHIPPER_UPDATE = "ROLE_SHIPPER_UPDATE";
    public static final String SHIPPER_DELETE = "ROLE_SHIPPER_DELETE";
    public static final String SHIPPER_SELF = "ROLE_SHIPPER_SELF";

    /* ---------- Đơn hàng ---------- */
    public static final String ORDER_VIEW = "ROLE_ORDER_VIEW";
    public static final String ORDER_CREATE = "ROLE_ORDER_CREATE";
    public static final String ORDER_UPDATE = "ROLE_ORDER_UPDATE";
    public static final String ORDER_CANCEL = "ROLE_ORDER_CANCEL";
    public static final String ORDER_CONFIRM = "ROLE_ORDER_CONFIRM";
    public static final String ORDER_IMPORT = "ROLE_ORDER_IMPORT";
    public static final String ORDER_EXPORT = "ROLE_ORDER_EXPORT";

    /* ---------- Điều phối ---------- */
    public static final String DISPATCH_VIEW = "ROLE_DISPATCH_VIEW";
    public static final String DISPATCH_ASSIGN = "ROLE_DISPATCH_ASSIGN";
    public static final String DISPATCH_RESPOND = "ROLE_DISPATCH_RESPOND";

    /* ---------- Tracking ---------- */
    public static final String TRACKING_VIEW = "ROLE_TRACKING_VIEW";
    public static final String TRACKING_PUSH = "ROLE_TRACKING_PUSH";

    /* ---------- Phí và voucher ---------- */
    public static final String PRICING_VIEW = "ROLE_PRICING_VIEW";
    public static final String PRICING_MANAGE = "ROLE_PRICING_MANAGE";
    public static final String VOUCHER_VIEW = "ROLE_VOUCHER_VIEW";
    public static final String VOUCHER_MANAGE = "ROLE_VOUCHER_MANAGE";

    /* ---------- Thanh toán ---------- */
    public static final String PAYMENT_VIEW = "ROLE_PAYMENT_VIEW";
    public static final String PAYMENT_CREATE = "ROLE_PAYMENT_CREATE";
    public static final String COD_VIEW = "ROLE_COD_VIEW";
    public static final String COD_SUBMIT = "ROLE_COD_SUBMIT";
    public static final String COD_CONFIRM = "ROLE_COD_CONFIRM";

    /* ---------- Dashboard ---------- */
    public static final String DASHBOARD_VIEW = "ROLE_DASHBOARD_VIEW";
    public static final String DASHBOARD_ADMIN = "ROLE_DASHBOARD_ADMIN";

    /* ---------- Biểu thức dùng lại trong @PreAuthorize ---------- */
    public static final String HAS_ORDER_VIEW = "hasAuthority('" + ORDER_VIEW + "')";
    public static final String HAS_ORDER_CREATE = "hasAuthority('" + ORDER_CREATE + "')";
    public static final String HAS_ORDER_UPDATE = "hasAuthority('" + ORDER_UPDATE + "')";
    public static final String HAS_ORDER_CANCEL = "hasAuthority('" + ORDER_CANCEL + "')";
    public static final String HAS_ORDER_CONFIRM = "hasAuthority('" + ORDER_CONFIRM + "')";
    public static final String HAS_ORDER_IMPORT = "hasAuthority('" + ORDER_IMPORT + "')";
    public static final String HAS_ORDER_EXPORT = "hasAuthority('" + ORDER_EXPORT + "')";
    public static final String HAS_USER_VIEW = "hasAuthority('" + USER_VIEW + "')";
    public static final String HAS_USER_CREATE = "hasAuthority('" + USER_CREATE + "')";
    public static final String HAS_USER_UPDATE = "hasAuthority('" + USER_UPDATE + "')";
    public static final String HAS_USER_DELETE = "hasAuthority('" + USER_DELETE + "')";
    public static final String HAS_ROLE_VIEW = "hasAuthority('" + ROLE_VIEW + "')";
    public static final String HAS_ROLE_MANAGE = "hasAuthority('" + ROLE_MANAGE + "')";
    public static final String HAS_SHIPPER_VIEW = "hasAuthority('" + SHIPPER_VIEW + "')";
    public static final String HAS_SHIPPER_CREATE = "hasAuthority('" + SHIPPER_CREATE + "')";
    public static final String HAS_SHIPPER_UPDATE = "hasAuthority('" + SHIPPER_UPDATE + "')";
    public static final String HAS_SHIPPER_DELETE = "hasAuthority('" + SHIPPER_DELETE + "')";
    public static final String HAS_SHIPPER_SELF = "hasAuthority('" + SHIPPER_SELF + "')";
    public static final String HAS_DISPATCH_VIEW = "hasAuthority('" + DISPATCH_VIEW + "')";
    public static final String HAS_DISPATCH_ASSIGN = "hasAuthority('" + DISPATCH_ASSIGN + "')";
    public static final String HAS_DISPATCH_RESPOND = "hasAuthority('" + DISPATCH_RESPOND + "')";
    public static final String HAS_TRACKING_VIEW = "hasAuthority('" + TRACKING_VIEW + "')";
    public static final String HAS_TRACKING_PUSH = "hasAuthority('" + TRACKING_PUSH + "')";
    public static final String HAS_PRICING_VIEW = "hasAuthority('" + PRICING_VIEW + "')";
    public static final String HAS_PRICING_MANAGE = "hasAuthority('" + PRICING_MANAGE + "')";
    public static final String HAS_VOUCHER_VIEW = "hasAuthority('" + VOUCHER_VIEW + "')";
    public static final String HAS_VOUCHER_MANAGE = "hasAuthority('" + VOUCHER_MANAGE + "')";
    public static final String HAS_PAYMENT_VIEW = "hasAuthority('" + PAYMENT_VIEW + "')";
    public static final String HAS_PAYMENT_CREATE = "hasAuthority('" + PAYMENT_CREATE + "')";
    public static final String HAS_COD_VIEW = "hasAuthority('" + COD_VIEW + "')";
    public static final String HAS_COD_SUBMIT = "hasAuthority('" + COD_SUBMIT + "')";
    public static final String HAS_COD_CONFIRM = "hasAuthority('" + COD_CONFIRM + "')";
    public static final String HAS_DASHBOARD_VIEW = "hasAuthority('" + DASHBOARD_VIEW + "')";
    public static final String HAS_DASHBOARD_ADMIN = "hasAuthority('" + DASHBOARD_ADMIN + "')";
}
