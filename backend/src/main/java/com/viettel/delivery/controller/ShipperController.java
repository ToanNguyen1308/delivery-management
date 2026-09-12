package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.dto.request.ShipperCreateRequest;
import com.viettel.delivery.dto.request.ShipperUpdateRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.ShipperPerformanceResponse;
import com.viettel.delivery.dto.response.ShipperResponse;
import com.viettel.delivery.dto.search.ShipperSearchRequest;
import com.viettel.delivery.service.ShipperService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shippers")
@RequiredArgsConstructor
@Tag(name = "04. Quan ly shipper", description = "Ho so, trang thai va hieu suat cua shipper")
public class ShipperController {

    private final ShipperService shipperService;
    private final MessageUtil messageUtil;

    @PostMapping("/search")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_VIEW)
    @Operation(summary = "Tim kiem shipper")
    public ResponseEntity<ApiResponse<PageResponse<ShipperResponse>>> search(
            @Valid @RequestBody ShipperSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(shipperService.search(request)));
    }

    @GetMapping("/available")
    @PreAuthorize(PermissionCode.HAS_DISPATCH_VIEW)
    @Operation(summary = "Danh sach shipper con cho nhan don", description = "Dung cho man hinh dieu phoi")
    public ResponseEntity<ApiResponse<List<ShipperResponse>>> getAvailable() {
        return ResponseEntity.ok(ApiResponse.success(shipperService.getAvailableShippers()));
    }

    @GetMapping("/me")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_SELF)
    @Operation(summary = "Ho so shipper cua tai khoan dang dang nhap")
    public ResponseEntity<ApiResponse<ShipperResponse>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(shipperService.getMyProfile()));
    }

    @PutMapping("/me/status")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_SELF)
    @Operation(summary = "Shipper tu doi trang thai ONLINE/OFFLINE")
    public ResponseEntity<ApiResponse<ShipperResponse>> updateMyStatus(@RequestParam ShipperStatus status) {
        return ResponseEntity.ok(ApiResponse.success(shipperService.updateMyStatus(status),
                messageUtil.get("success.common.updated")));
    }

    @GetMapping("/me/performance")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_SELF)
    @Operation(summary = "Hieu suat giao hang cua chinh minh")
    public ResponseEntity<ApiResponse<ShipperPerformanceResponse>> getMyPerformance() {
        return ResponseEntity.ok(ApiResponse.success(shipperService.getMyPerformance()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_VIEW)
    @Operation(summary = "Chi tiet shipper")
    public ResponseEntity<ApiResponse<ShipperResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(shipperService.getById(id)));
    }

    @GetMapping("/{id}/performance")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_VIEW)
    @Operation(summary = "Hieu suat giao hang cua mot shipper")
    public ResponseEntity<ApiResponse<ShipperPerformanceResponse>> getPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(shipperService.getPerformance(id)));
    }

    @PostMapping
    @PreAuthorize(PermissionCode.HAS_SHIPPER_CREATE)
    @Operation(summary = "Tao ho so shipper")
    public ResponseEntity<ApiResponse<ShipperResponse>> create(@Valid @RequestBody ShipperCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(shipperService.create(request), messageUtil.get("success.common.created")));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_UPDATE)
    @Operation(summary = "Cap nhat ho so shipper")
    public ResponseEntity<ApiResponse<ShipperResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody ShipperUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(shipperService.update(id, request),
                messageUtil.get("success.common.updated")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_SHIPPER_DELETE)
    @Operation(summary = "Xoa ho so shipper")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        shipperService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.deleted")));
    }
}
