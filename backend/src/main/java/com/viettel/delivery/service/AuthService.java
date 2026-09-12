package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.ChangePasswordRequest;
import com.viettel.delivery.dto.request.LoginRequest;
import com.viettel.delivery.dto.request.RefreshTokenRequest;
import com.viettel.delivery.dto.request.RegisterRequest;
import com.viettel.delivery.dto.response.LoginResponse;
import com.viettel.delivery.dto.response.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void logout(String accessToken);

    UserResponse getCurrentProfile();

    void changePassword(ChangePasswordRequest request);
}
