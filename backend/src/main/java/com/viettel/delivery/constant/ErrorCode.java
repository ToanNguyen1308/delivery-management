package com.viettel.delivery.constant;

/**
 * Ma loi chuan hoa. Gia tri chinh la key trong file i18n (messages_vi/messages_en).
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    /* ---------- Chung ---------- */
    public static final String INTERNAL_ERROR = "error.common.internal";
    public static final String VALIDATION_FAILED = "error.common.validation";
    public static final String BAD_REQUEST = "error.common.badRequest";
    public static final String ACCESS_DENIED = "error.common.accessDenied";
    public static final String UNAUTHORIZED = "error.common.unauthorized";
    public static final String RESOURCE_NOT_FOUND = "error.common.notFound";
    public static final String FILE_TOO_LARGE = "error.common.fileTooLarge";

    /* ---------- Nguoi dung ---------- */
    public static final String USER_NOT_FOUND = "error.user.notFound";
    public static final String USER_USERNAME_EXISTED = "error.user.usernameExisted";
    public static final String USER_EMAIL_EXISTED = "error.user.emailExisted";
    public static final String USER_PHONE_EXISTED = "error.user.phoneExisted";
    public static final String USER_INACTIVE = "error.user.inactive";
    public static final String USER_BAD_CREDENTIALS = "error.user.badCredentials";
    public static final String USER_OLD_PASSWORD_INVALID = "error.user.oldPasswordInvalid";
    public static final String USER_CANNOT_DELETE_SELF = "error.user.cannotDeleteSelf";

    /* ---------- Token ---------- */
    public static final String TOKEN_INVALID = "error.token.invalid";
    public static final String TOKEN_EXPIRED = "error.token.expired";
    public static final String TOKEN_REVOKED = "error.token.revoked";

    /* ---------- Nhom quyen ---------- */
    public static final String ROLE_GROUP_NOT_FOUND = "error.roleGroup.notFound";
    public static final String ROLE_GROUP_CODE_EXISTED = "error.roleGroup.codeExisted";
    public static final String FUNCTION_NOT_FOUND = "error.function.notFound";

    /* ---------- Shipper ---------- */
    public static final String SHIPPER_NOT_FOUND = "error.shipper.notFound";
    public static final String SHIPPER_CODE_EXISTED = "error.shipper.codeExisted";
    public static final String SHIPPER_USER_EXISTED = "error.shipper.userExisted";
    public static final String SHIPPER_NOT_AVAILABLE = "error.shipper.notAvailable";
    public static final String SHIPPER_OVERLOADED = "error.shipper.overloaded";
    public static final String SHIPPER_PROFILE_MISSING = "error.shipper.profileMissing";

    /* ---------- Don hang ---------- */
    public static final String ORDER_NOT_FOUND = "error.order.notFound";
    public static final String ORDER_INVALID_TRANSITION = "error.order.invalidTransition";
    public static final String ORDER_NOT_EDITABLE = "error.order.notEditable";
    public static final String ORDER_ALREADY_ASSIGNED = "error.order.alreadyAssigned";
    public static final String ORDER_NOT_ASSIGNED = "error.order.notAssigned";
    public static final String ORDER_NOT_PAID = "error.order.notPaid";
    public static final String ORDER_ALREADY_PAID = "error.order.alreadyPaid";
    public static final String ORDER_PROOF_REQUIRED = "error.order.proofRequired";

    /* ---------- Dieu phoi ---------- */
    public static final String ASSIGNMENT_NOT_FOUND = "error.assignment.notFound";
    public static final String ASSIGNMENT_NOT_PENDING = "error.assignment.notPending";
    public static final String ASSIGNMENT_NO_SHIPPER_AVAILABLE = "error.assignment.noShipperAvailable";

    /* ---------- Phi va voucher ---------- */
    public static final String PRICING_RULE_NOT_FOUND = "error.pricing.notFound";
    public static final String VOUCHER_NOT_FOUND = "error.voucher.notFound";
    public static final String VOUCHER_CODE_EXISTED = "error.voucher.codeExisted";
    public static final String VOUCHER_EXPIRED = "error.voucher.expired";
    public static final String VOUCHER_NOT_STARTED = "error.voucher.notStarted";
    public static final String VOUCHER_OUT_OF_QUANTITY = "error.voucher.outOfQuantity";
    public static final String VOUCHER_MIN_ORDER_NOT_MET = "error.voucher.minOrderNotMet";
    public static final String VOUCHER_ALREADY_USED = "error.voucher.alreadyUsed";
    public static final String VOUCHER_INACTIVE = "error.voucher.inactive";

    /* ---------- Thanh toan ---------- */
    public static final String PAYMENT_NOT_FOUND = "error.payment.notFound";
    public static final String PAYMENT_GATEWAY_DISABLED = "error.payment.gatewayDisabled";
    public static final String PAYMENT_INVALID_SIGNATURE = "error.payment.invalidSignature";
    public static final String PAYMENT_METHOD_NOT_SUPPORTED = "error.payment.methodNotSupported";
    public static final String PAYMENT_AMOUNT_MISMATCH = "error.payment.amountMismatch";
    public static final String PAYMENT_ALREADY_PROCESSED = "error.payment.alreadyProcessed";
    public static final String COD_SETTLEMENT_NOT_FOUND = "error.cod.notFound";
    public static final String COD_NOTHING_TO_SETTLE = "error.cod.nothingToSettle";

    /* ---------- File ---------- */
    public static final String FILE_UPLOAD_FAILED = "error.file.uploadFailed";
    public static final String FILE_NOT_FOUND = "error.file.notFound";
    public static final String FILE_TYPE_NOT_ALLOWED = "error.file.typeNotAllowed";

    /* ---------- Excel ---------- */
    public static final String EXCEL_INVALID_FORMAT = "error.excel.invalidFormat";
    public static final String EXCEL_EMPTY = "error.excel.empty";
    public static final String EXCEL_TOO_MANY_ROWS = "error.excel.tooManyRows";
    public static final String EXCEL_EXPORT_FAILED = "error.excel.exportFailed";
}
