package com.viettel.delivery.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class VnPayUtilTest {

    private static final String SECRET = "VIETTELDELIVERYSANDBOXSECRET";

    private Map<String, String> samplePayload() {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "DEMO0001");
        params.put("vnp_Amount", "3232000");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", "TXN20260911120000123");
        params.put("vnp_OrderInfo", "Thanh toan don hang DH260911ABCDEF");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", "http://localhost:3000/payment/result");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", "20260911120000");
        return params;
    }

    @Test
    @DisplayName("Chuoi du lieu ky duoc sap xep theo thu tu alphabet cua ten tham so")
    void shouldSortFieldsAlphabetically() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Amount", "1000");
        params.put("vnp_Command", "pay");

        String hashData = VnPayUtil.buildHashData(params);

        assertThat(hashData).isEqualTo("vnp_Amount=1000&vnp_Command=pay&vnp_Version=2.1.0");
    }

    @Test
    @DisplayName("Tham so rong bi loai khoi chuoi ky")
    void shouldSkipEmptyValues() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Amount", "1000");
        params.put("vnp_BankCode", "");
        params.put("vnp_CardType", null);

        assertThat(VnPayUtil.buildHashData(params)).isEqualTo("vnp_Amount=1000");
    }

    @Test
    @DisplayName("Truong chu ky khong duoc tinh vao chuoi ky")
    void shouldExcludeSecureHashFields() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Amount", "1000");
        params.put(VnPayUtil.SECURE_HASH_FIELD, "abcdef");
        params.put(VnPayUtil.SECURE_HASH_TYPE_FIELD, "SHA512");

        assertThat(VnPayUtil.buildHashData(params)).isEqualTo("vnp_Amount=1000");
    }

    @Test
    @DisplayName("Gia tri co dau cach duoc ma hoa URL truoc khi ky")
    void shouldUrlEncodeValues() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_OrderInfo", "Thanh toan don hang DH001");

        assertThat(VnPayUtil.buildHashData(params)).isEqualTo("vnp_OrderInfo=Thanh+toan+don+hang+DH001");
    }

    @Test
    @DisplayName("Chu ky HMAC-SHA512 dai 128 ky tu hex va on dinh")
    void shouldProduceStableHexSignature() {
        String data = VnPayUtil.buildHashData(samplePayload());

        String first = VnPayUtil.hmacSha512(SECRET, data);
        String second = VnPayUtil.hmacSha512(SECRET, data);

        assertThat(first).hasSize(128).matches("[0-9a-f]{128}").isEqualTo(second);
    }

    @Test
    @DisplayName("Doi secret thi chu ky thay doi")
    void shouldProduceDifferentSignatureForDifferentSecret() {
        String data = VnPayUtil.buildHashData(samplePayload());

        assertThat(VnPayUtil.hmacSha512(SECRET, data))
                .isNotEqualTo(VnPayUtil.hmacSha512("SECRET_KHAC", data));
    }

    @Test
    @DisplayName("Xac thuc thanh cong voi chu ky dung, khong phan biet chu hoa chu thuong")
    void shouldVerifyValidSignature() {
        Map<String, String> params = samplePayload();
        String signature = VnPayUtil.hmacSha512(SECRET, VnPayUtil.buildHashData(params));

        assertThat(VnPayUtil.verifySignature(SECRET, params, signature)).isTrue();
        assertThat(VnPayUtil.verifySignature(SECRET, params, signature.toUpperCase())).isTrue();
    }

    @Test
    @DisplayName("Du lieu bi sua thi xac thuc chu ky that bai")
    void shouldRejectTamperedData() {
        Map<String, String> params = samplePayload();
        String signature = VnPayUtil.hmacSha512(SECRET, VnPayUtil.buildHashData(params));

        Map<String, String> tampered = new HashMap<>(params);
        tampered.put("vnp_Amount", "100");

        assertThat(VnPayUtil.verifySignature(SECRET, tampered, signature)).isFalse();
    }

    @Test
    @DisplayName("Thieu chu ky thi xac thuc that bai")
    void shouldRejectMissingSignature() {
        Map<String, String> params = samplePayload();

        assertThat(VnPayUtil.verifySignature(SECRET, params, null)).isFalse();
        assertThat(VnPayUtil.verifySignature(SECRET, params, "  ")).isFalse();
    }
}
