package com.viettel.delivery.constant;

import java.math.BigDecimal;

/**
 * Hang so dung chung toan he thong. Khong hard-code magic number/string trong service.
 */
public final class AppConstants {

    private AppConstants() {
    }

    /* ---------- Response ---------- */
    public static final String RESPONSE_CODE_SUCCESS = "success";
    public static final String RESPONSE_MESSAGE_SUCCESS = "Success";

    /* ---------- Bao mat ---------- */
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_AUTHORITIES = "authorities";
    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String SYSTEM_AUDITOR = "system";
    public static final String ANONYMOUS_USER = "anonymousUser";

    /* ---------- Redis key ---------- */
    public static final String REDIS_BLACKLIST_PREFIX = "auth:blacklist:";
    public static final String CACHE_DASHBOARD = "dashboard";
    public static final String CACHE_PRICING = "pricing";

    /* ---------- Phan trang ---------- */
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 200;
    public static final String DEFAULT_SORT_FIELD = "createdDate";
    public static final String SORT_DESC = "DESC";

    /* ---------- WebSocket ---------- */
    public static final String WS_ENDPOINT = "/ws";
    public static final String WS_TOPIC_ORDER = "/topic/orders/";
    public static final String WS_TOPIC_SHIPPER_LOCATION = "/topic/shippers/location";
    public static final String WS_TOPIC_DISPATCH = "/topic/dispatch";
    public static final String WS_QUEUE_NOTIFICATION = "/queue/notifications";
    public static final String WS_USER_DESTINATION_PREFIX = "/user";

    /* ---------- MinIO ---------- */
    public static final String MINIO_TEMP_FOLDER = "temp";
    public static final String MINIO_FOLDER_DELIVERY_PROOF = "delivery-proof";
    public static final String MINIO_FOLDER_SHIPPER_DOCUMENT = "shipper-document";
    public static final String MINIO_FOLDER_AVATAR = "avatar";

    /* ---------- Nghiep vu ---------- */
    public static final String ORDER_CODE_PREFIX = "DH";
    public static final String PAYMENT_TXN_PREFIX = "TXN";
    public static final double EARTH_RADIUS_KM = 6371.0;
    public static final BigDecimal VND_MULTIPLIER = BigDecimal.valueOf(100);
    public static final int MONEY_SCALE = 0;
    public static final int DISTANCE_SCALE = 2;
    public static final int MAX_ASSIGN_CANDIDATES = 20;
    public static final int DEFAULT_DELIVERY_SLA_HOURS = 48;

    /* ---------- Excel ---------- */
    public static final int EXCEL_HEADER_ROW_INDEX = 0;
    public static final int EXCEL_MAX_IMPORT_ROWS = 1000;
}
