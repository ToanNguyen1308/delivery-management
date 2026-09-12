package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.request.CreatePaymentRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.PaymentInitResponse;
import com.viettel.delivery.dto.response.PaymentResponse;
import com.viettel.delivery.dto.response.PaymentResultResponse;
import com.viettel.delivery.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "09. Thanh toan", description = "Thanh toan online qua VNPay va tra cuu giao dich")
public class PaymentController {

    private static final String HEADER_FORWARDED_FOR = "X-FORWARDED-FOR";

    private final PaymentService paymentService;

    @PostMapping("/create")
    @PreAuthorize(PermissionCode.HAS_PAYMENT_CREATE)
    @Operation(summary = "Tao giao dich thanh toan",
            description = "Tra ve payUrl de frontend chuyen huong nguoi dung sang cong thanh toan")
    public ResponseEntity<ApiResponse<PaymentInitResponse>> create(@Valid @RequestBody CreatePaymentRequest request,
                                                                   HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.createPayment(request, resolveClientIp(httpRequest))));
    }

    @GetMapping("/vnpay/return")
    @Operation(summary = "Xu ly ReturnUrl cua VNPay",
            description = "Frontend goi endpoint nay kem toan bo query param nhan duoc tu VNPay")
    public ResponseEntity<ApiResponse<PaymentResultResponse>> handleReturn(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.handleReturn(extractParams(httpRequest))));
    }

    /**
     * Endpoint nay duoc VNPay goi truc tiep server-to-server nen khong yeu cau dang nhap.
     */
    @GetMapping("/vnpay/ipn")
    @Operation(summary = "Nhan IPN tu VNPay", description = "Tra ve RspCode theo dinh dang VNPay quy dinh")
    public ResponseEntity<Map<String, String>> handleIpn(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(paymentService.handleIpn(extractParams(httpRequest)));
    }

    @PostMapping("/mock/complete")
    @PreAuthorize(PermissionCode.HAS_PAYMENT_CREATE)
    @Operation(summary = "Hoan tat thanh toan gia lap",
            description = "Chi dung khi he thong chua cau hinh VNPay sandbox, phuc vu demo offline")
    public ResponseEntity<ApiResponse<PaymentResultResponse>> completeMock(
            @RequestParam String txnRef,
            @RequestParam(defaultValue = "true") boolean success) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.completeMockPayment(txnRef, success)));
    }

    @PostMapping("/search")
    @PreAuthorize(PermissionCode.HAS_PAYMENT_VIEW)
    @Operation(summary = "Lich su giao dich thanh toan")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> search(
            @Valid @RequestBody PaymentSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.search(request)));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize(PermissionCode.HAS_PAYMENT_VIEW)
    @Operation(summary = "Giao dich thanh toan cua mot don hang")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByOrder(orderId)));
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(HEADER_FORWARDED_FOR);
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }

    @Getter
    @Setter
    public static class PaymentSearchRequest extends BaseSearchRequest {
    }
}
