package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@Schema(name = "LoginResponse", description = "Ket qua dang nhap")
public class LoginResponse implements Serializable {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Thoi gian song cua access token (ms)")
    private Long expiresIn;

    private UserResponse user;

    @Schema(description = "Danh sach function_code de frontend render menu theo quyen")
    private List<String> permissions;

    @Schema(description = "Danh sach ma nhom quyen cua nguoi dung")
    private List<String> roleGroups;
}
