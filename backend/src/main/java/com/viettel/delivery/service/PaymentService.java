package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.request.CreatePaymentRequest;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.PaymentInitResponse;
import com.viettel.delivery.dto.response.PaymentResponse;
import com.viettel.delivery.dto.response.PaymentResultResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    PaymentInitResponse createPayment(CreatePaymentRequest request, String clientIp);

    /**
     * Xu ly ReturnUrl: nguoi dung duoc chuyen ve frontend kem ket qua giao dich.
     */
    PaymentResultResponse handleReturn(Map<String, String> params);

    /**
     * Xu ly IPN server-to-server. Tra ve ma phan hoi theo dinh dang VNPay yeu cau.
     */
    Map<String, String> handleIpn(Map<String, String> params);

    /**
     * Hoan tat giao dich gia lap khi chua cau hinh VNPay sandbox.
     */
    PaymentResultResponse completeMockPayment(String txnRef, boolean success);

    PageResponse<PaymentResponse> search(BaseSearchRequest request);

    List<PaymentResponse> getByOrder(Long orderId);
}
