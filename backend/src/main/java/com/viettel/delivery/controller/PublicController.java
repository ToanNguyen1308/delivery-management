package com.viettel.delivery.controller;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.EnumResponse;
import com.viettel.delivery.dto.response.OrderTrackingResponse;
import com.viettel.delivery.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Cac API khong yeu cau dang nhap, dung cho trang tra cuu van don cong khai.
 */
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "00. Cong khai", description = "Tra cuu van don va danh muc dung chung, khong can dang nhap")
public class PublicController {

    private final TrackingService trackingService;

    @GetMapping("/tracking/{orderCode}")
    @Operation(summary = "Tra cuu van don theo ma",
            description = "Thong tin nguoi nhan va dia chi duoc che bot de bao ve du lieu ca nhan")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> track(@PathVariable String orderCode) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.getPublicTracking(orderCode)));
    }

    @GetMapping("/enums")
    @Operation(summary = "Danh muc enum dung cho dropdown tren frontend")
    public ResponseEntity<ApiResponse<Map<String, List<EnumResponse>>>> getEnums() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "orderStatuses", EnumResponse.listOf(OrderStatus.class),
                "serviceTypes", EnumResponse.listOf(ServiceType.class),
                "paymentMethods", EnumResponse.listOf(PaymentMethod.class))));
    }
}
