package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.FeePreviewRequest;
import com.viettel.delivery.dto.request.PricingRuleRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.FeePreviewResponse;
import com.viettel.delivery.dto.response.PricingRuleResponse;
import com.viettel.delivery.service.PricingService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
@Tag(name = "05. Tinh phi", description = "Cau hinh bang phi va tinh thu cuoc van chuyen")
public class PricingController {

    private final PricingService pricingService;
    private final MessageUtil messageUtil;

    @PostMapping("/preview")
    @PreAuthorize(PermissionCode.HAS_PRICING_VIEW)
    @Operation(summary = "Tinh thu cuoc van chuyen",
            description = "Tra ve chi tiet tung thanh phan phi va so tien duoc giam neu co voucher")
    public ResponseEntity<ApiResponse<FeePreviewResponse>> preview(@Valid @RequestBody FeePreviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pricingService.preview(request)));
    }

    @GetMapping("/rules")
    @PreAuthorize(PermissionCode.HAS_PRICING_VIEW)
    @Operation(summary = "Danh sach bang phi theo loai dich vu")
    public ResponseEntity<ApiResponse<List<PricingRuleResponse>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.success(pricingService.getAllRules()));
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize(PermissionCode.HAS_PRICING_MANAGE)
    @Operation(summary = "Cap nhat bang phi")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> updateRule(@PathVariable Long id,
                                                                       @Valid @RequestBody PricingRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(pricingService.updateRule(id, request),
                messageUtil.get("success.common.updated")));
    }
}
