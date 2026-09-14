package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(name = "UserResponse", description = "Thong tin nguoi dung")
public class UserResponse implements Serializable {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String identityNumber;
    private LocalDate birthday;
    private String address;
    private String avatarUrl;
    private EnumResponse status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdDate;
    private List<RoleGroupSummaryResponse> roleGroups;

    @Schema(description = "Danh sách function_code, dùng khi F5 để không mất menu")
    private List<String> permissions;
}
