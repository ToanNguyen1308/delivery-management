package com.viettel.delivery.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

/** Ký HMAC-SHA512 theo chuẩn VNPay 2.1.0. */
public final class VnPayUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA512";
    public static final String SECURE_HASH_FIELD = "vnp_SecureHash";
    public static final String SECURE_HASH_TYPE_FIELD = "vnp_SecureHashType";

    private VnPayUtil() {
    }

    /** Sắp tham số alphabet rồi nối chuỗi canonical để ký. */
    public static String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        fieldNames.removeIf(name -> SECURE_HASH_FIELD.equals(name) || SECURE_HASH_TYPE_FIELD.equals(name));
        fieldNames.sort(String::compareTo);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (!hashData.isEmpty()) {
                hashData.append('&');
            }
            hashData.append(fieldName)
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
        }
        return hashData.toString();
    }

    /** Query gửi VNPay phải cùng thứ tự với chuỗi đã ký. */
    public static String buildQueryUrl(Map<String, String> params) {
        return buildHashData(params);
    }

    public static String hmacSha512(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHexString(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Khong the tao chu ky HMAC-SHA512", ex);
        }
    }

    public static boolean verifySignature(String secretKey, Map<String, String> params, String receivedHash) {
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }
        String expected = hmacSha512(secretKey, buildHashData(params));
        return expected.equalsIgnoreCase(receivedHash);
    }

    private static String toHexString(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }
}
