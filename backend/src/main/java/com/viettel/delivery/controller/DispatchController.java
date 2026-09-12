package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.constant.enums.AssignmentStatus;
import com.viettel.delivery.dto.request.AssignOrderRequest;
import com.viettel.delivery.dto.request.AssignmentResponseRequest;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.AssignmentResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.service.DispatchService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dispatch")
@RequiredArgsConstructor
@Tag(name = "08. Dieu phoi", description = "Phan cong don hang cho shipper va xu ly phan hoi")
public class DispatchController {

    private final DispatchService dispatchService;
    private final MessageUtil messageUtil;

    @PostMapping("/assign")
    @PreAuthorize(PermissionCode.HAS_DISPATCH_ASSIGN)
    @Operation(summary = "Phan cong don hang",
            description = "Bo trong shipperId de he thong tu chon shipper phu hop theo chien luoc cau hinh")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assign(@Valid @RequestBody AssignOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.assign(request),
                messageUtil.get("success.common.updated")));
    }

    @PutMapping("/assignments/{id}/respond")
    @PreAuthorize(PermissionCode.HAS_DISPATCH_RESPOND)
    @Operation(summary = "Shipper nhan hoac tu choi don duoc phan cong",
            description = "Khi tu choi, don tu dong quay lai hang cho de dieu phoi vien gan lai")
    public ResponseEntity<ApiResponse<AssignmentResponse>> respond(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentResponseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.respond(id, request),
                messageUtil.get("success.common.updated")));
    }

    @PostMapping("/my-assignments")
    @PreAuthorize(PermissionCode.HAS_DISPATCH_RESPOND)
    @Operation(summary = "Danh sach nhiem vu cua shipper dang dang nhap")
    public ResponseEntity<ApiResponse<PageResponse<AssignmentResponse>>> getMyAssignments(
            @Valid @RequestBody AssignmentSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                dispatchService.getMyAssignments(request, request.getStatuses())));
    }

    @GetMapping("/orders/{orderId}/assignments")
    @PreAuthorize(PermissionCode.HAS_DISPATCH_VIEW)
    @Operation(summary = "Lich su phan cong cua mot don hang")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(dispatchService.getAssignmentsByOrder(orderId)));
    }

    @Getter
    @Setter
    public static class AssignmentSearchRequest extends BaseSearchRequest {
        private List<AssignmentStatus> statuses;
    }
}
