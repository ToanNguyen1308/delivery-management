package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.VoucherRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.VoucherResponse;
import com.viettel.delivery.dto.search.VoucherSearchRequest;
import com.viettel.delivery.service.VoucherService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@Tag(name = "06. Voucher", description = "Quan ly ma giam gia")
public class VoucherController {

    private final VoucherService voucherService;
    private final MessageUtil messageUtil;

    @PostMapping("/search")
    @PreAuthorize(PermissionCode.HAS_VOUCHER_VIEW)
    @Operation(summary = "Tim kiem voucher")
    public ResponseEntity<ApiResponse<PageResponse<VoucherResponse>>> search(
            @Valid @RequestBody VoucherSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.search(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_VOUCHER_VIEW)
    @Operation(summary = "Chi tiet voucher")
    public ResponseEntity<ApiResponse<VoucherResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.getById(id)));
    }

    @PostMapping
    @PreAuthorize(PermissionCode.HAS_VOUCHER_MANAGE)
    @Operation(summary = "Tao voucher")
    public ResponseEntity<ApiResponse<VoucherResponse>> create(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(voucherService.create(request), messageUtil.get("success.common.created")));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_VOUCHER_MANAGE)
    @Operation(summary = "Cap nhat voucher")
    public ResponseEntity<ApiResponse<VoucherResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(ApiResponse.success(voucherService.update(id, request),
                messageUtil.get("success.common.updated")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_VOUCHER_MANAGE)
    @Operation(summary = "Xoa voucher")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        voucherService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.deleted")));
    }
}
