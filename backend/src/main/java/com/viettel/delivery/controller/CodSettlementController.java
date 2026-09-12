package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.constant.enums.CodSettlementStatus;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.CodSettlementResponse;
import com.viettel.delivery.dto.response.CodWalletResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.service.CodSettlementService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/cod-settlements")
@RequiredArgsConstructor
@Tag(name = "10. Doi soat COD", description = "Vi tien thu ho cua shipper va quy trinh doi soat")
public class CodSettlementController {

    private final CodSettlementService codSettlementService;
    private final MessageUtil messageUtil;

    @GetMapping("/my-wallet")
    @PreAuthorize(PermissionCode.HAS_COD_VIEW)
    @Operation(summary = "Vi COD cua shipper dang dang nhap")
    public ResponseEntity<ApiResponse<CodWalletResponse>> getMyWallet() {
        return ResponseEntity.ok(ApiResponse.success(codSettlementService.getMyWallet()));
    }

    @PostMapping("/my-settlements")
    @PreAuthorize(PermissionCode.HAS_COD_VIEW)
    @Operation(summary = "Danh sach khoan COD cua shipper dang dang nhap")
    public ResponseEntity<ApiResponse<PageResponse<CodSettlementResponse>>> searchMine(
            @Valid @RequestBody CodSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                codSettlementService.searchMySettlements(request, request.getStatuses())));
    }

    @PostMapping("/search")
    @PreAuthorize(PermissionCode.HAS_COD_CONFIRM)
    @Operation(summary = "Danh sach khoan COD toan he thong")
    public ResponseEntity<ApiResponse<PageResponse<CodSettlementResponse>>> search(
            @Valid @RequestBody CodSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                codSettlementService.search(request, request.getStatuses())));
    }

    @PostMapping("/submit-all")
    @PreAuthorize(PermissionCode.HAS_COD_SUBMIT)
    @Operation(summary = "Shipper nop toan bo tien COD dang giu")
    public ResponseEntity<ApiResponse<Integer>> submitAll() {
        return ResponseEntity.ok(ApiResponse.success(codSettlementService.submitAllHolding(),
                messageUtil.get("success.common.updated")));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize(PermissionCode.HAS_COD_CONFIRM)
    @Operation(summary = "Xac nhan da nhan du tien COD")
    public ResponseEntity<ApiResponse<CodSettlementResponse>> confirm(@PathVariable Long id,
                                                                      @RequestParam(required = false) String note) {
        return ResponseEntity.ok(ApiResponse.success(codSettlementService.confirm(id, note),
                messageUtil.get("success.common.updated")));
    }

    @Getter
    @Setter
    public static class CodSearchRequest extends BaseSearchRequest {
        private List<CodSettlementStatus> statuses;
    }
}
