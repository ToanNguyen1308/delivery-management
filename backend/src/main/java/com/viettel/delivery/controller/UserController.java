package com.viettel.delivery.controller;

import com.viettel.delivery.constant.PermissionCode;
import com.viettel.delivery.dto.request.UserCreateRequest;
import com.viettel.delivery.dto.request.UserUpdateRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.PageResponse;
import com.viettel.delivery.dto.response.UserResponse;
import com.viettel.delivery.dto.search.UserSearchRequest;
import com.viettel.delivery.service.UserService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "02. Quan ly nguoi dung", description = "CRUD nguoi dung va gan nhom quyen")
public class UserController {

    private final UserService userService;
    private final MessageUtil messageUtil;

    @PostMapping("/search")
    @PreAuthorize(PermissionCode.HAS_USER_VIEW)
    @Operation(summary = "Tim kiem nguoi dung", description = "Ho tro tim theo tu khoa, trang thai, nhom quyen va khoang ngay tao")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> search(
            @Valid @RequestBody UserSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.search(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_USER_VIEW)
    @Operation(summary = "Chi tiet nguoi dung")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @PostMapping
    @PreAuthorize(PermissionCode.HAS_USER_CREATE)
    @Operation(summary = "Tao nguoi dung moi")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.create(request), messageUtil.get("success.common.created")));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_USER_UPDATE)
    @Operation(summary = "Cap nhat nguoi dung")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.update(id, request),
                messageUtil.get("success.common.updated")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PermissionCode.HAS_USER_DELETE)
    @Operation(summary = "Xoa nguoi dung (xoa mem)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.deleted")));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize(PermissionCode.HAS_USER_UPDATE)
    @Operation(summary = "Dat lai mat khau cho nguoi dung")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestParam @NotBlank @Size(min = 6, max = 100) String newPassword) {
        userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.updated")));
    }
}
