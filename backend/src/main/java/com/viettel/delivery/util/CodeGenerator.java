package com.viettel.delivery.util;

import com.viettel.delivery.constant.AppConstants;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Sinh ma nghiep vu (ma van don, ma giao dich) theo dinh dang de doc.
 */
public final class CodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private CodeGenerator() {
    }

    public static String orderCode() {
        return AppConstants.ORDER_CODE_PREFIX
                + LocalDateTime.now().format(DATE_FORMAT)
                + randomAlphaNumeric(6);
    }

    public static String paymentTxnRef() {
        return AppConstants.PAYMENT_TXN_PREFIX
                + LocalDateTime.now().format(TIMESTAMP_FORMAT)
                + randomDigits(4);
    }

    public static String shipperCode(long sequence) {
        return String.format("SP%05d", sequence);
    }

    public static String randomAlphaNumeric(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }

    public static String randomDigits(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
