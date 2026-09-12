package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.LocationUpdateRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.OrderTrackingResponse;
import com.viettel.delivery.dto.response.ShipperLocationResponse;
import com.viettel.delivery.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
@Tag(name = "12. Tracking", description = "Cap nhat vi tri shipper va theo doi hanh trinh don hang")
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/location")
    @PreAuthorize(PermissionCode.HAS_TRACKING_PUSH)
    @Operation(summary = "Shipper gui vi tri GPS",
            description = "Vi tri duoc luu lai va day realtime qua WebSocket topic /topic/orders/{orderCode}")
    public ResponseEntity<ApiResponse<ShipperLocationResponse>> pushLocation(
            @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.pushLocation(request)));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize(PermissionCode.HAS_TRACKING_VIEW)
    @Operation(summary = "Hanh trinh chi tiet cua don hang",
            description = "Gom timeline su kien va cac diem GPS de ve duong di tren ban do")
    public ResponseEntity<ApiResponse<OrderTrackingResponse>> getTracking(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(trackingService.getTrackingById(orderId)));
    }
}
