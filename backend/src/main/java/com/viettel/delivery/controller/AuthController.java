package com.viettel.delivery.controller;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.dto.request.ChangePasswordRequest;
import com.viettel.delivery.dto.request.LoginRequest;
import com.viettel.delivery.dto.request.RefreshTokenRequest;
import com.viettel.delivery.dto.request.RegisterRequest;
import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.LoginResponse;
import com.viettel.delivery.dto.response.UserResponse;
import com.viettel.delivery.service.AuthService;
import com.viettel.delivery.util.MessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "01. Xac thuc", description = "Dang nhap, dang ky, lam moi token va doi mat khau")
public class AuthController {

    private final AuthService authService;
    private final MessageUtil messageUtil;

    @PostMapping("/login")
    @Operation(summary = "Dang nhap", description = "Tra ve access token, refresh token va danh sach quyen")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request),
                messageUtil.get("success.auth.login")));
    }

    @PostMapping("/register")
    @Operation(summary = "Dang ky tai khoan khach hang")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authService.register(request), messageUtil.get("success.auth.register")));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Lam moi access token bang refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Dang xuat", description = "Thu hoi access token hien tai va toan bo refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = AppConstants.AUTH_HEADER, required = false) String authorizationHeader) {
        String accessToken = authorizationHeader != null && authorizationHeader.startsWith(AppConstants.TOKEN_PREFIX)
                ? authorizationHeader.substring(AppConstants.TOKEN_PREFIX.length())
                : null;
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.auth.logout")));
    }

    @GetMapping("/me")
    @Operation(summary = "Thong tin tai khoan dang dang nhap")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success(authService.getCurrentProfile()));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Doi mat khau")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(messageUtil.get("success.common.updated")));
    }
}
