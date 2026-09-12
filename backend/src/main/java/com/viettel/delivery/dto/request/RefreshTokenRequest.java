package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "RefreshTokenRequest", description = "Lam moi access token")
public class RefreshTokenRequest implements Serializable {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}
