package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.RoleGroupRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.FunctionResponse;
import com.viettel.delivery.dto.response.RoleGroupResponse;
import com.viettel.delivery.service.RoleGroupService;
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

import java.util.List;

@RestController
@RequestMapping("/role-groups")
@RequiredArgsConstructor
@Tag(name = "03. Nhom quyen", description = "Quan ly nhom quyen va chuc nang he thong")
public class RoleGroupController {

    private final RoleGroupService roleGroupService;
    private final MessageUtil messageUtil;

    @GetMapping
    @PreAuthorize(PermissionCode.HAS_ROLE_VIEW)
    @Operation(summary = "Danh sach nhom quyen kem chuc nang")
    public ResponseEntity<ApiResponse<List<RoleGroupResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(roleGroupService.getAll()));
    }

    @GetMapping("/functions")
    @PreAuthorize(PermissionCode.HAS_ROLE_VIEW)
    @Operation(summary = "Danh sach toan bo chuc nang he thong")
    public ResponseEntity<ApiResponse<List<FunctionResponse>>> getAllFunctions() {
        return ResponseEntity.ok(ApiResponse.success(roleGroupService.getAllFunctions()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_ROLE_VIEW)
    @Operation(summary = "Chi tiet nhom quyen")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleGroupService.getById(id)));
    }

    @PostMapping
    @PreAuthorize(PermissionCode.HAS_ROLE_MANAGE)
    @Operation(summary = "Tao nhom quyen")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> create(@Valid @RequestBody RoleGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roleGroupService.create(request), messageUtil.get("success.common.created")));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_ROLE_MANAGE)
    @Operation(summary = "Cap nhat nhom quyen")
    public ResponseEntity<ApiResponse<RoleGroupResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody RoleGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleGroupService.update(id, request),
                messageUtil.get("success.common.updated")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_ROLE_MANAGE)
    @Operation(summary = "Xoa nhom quyen")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleGroupService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.deleted")));
    }
}
