package com.viettel.delivery.dto.search;

import com.viettel.delivery.constant.enums.UserStatus;
import com.viettel.delivery.dto.request.BaseSearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(name = "UserSearchRequest", description = "Dieu kien tim kiem nguoi dung")
public class UserSearchRequest extends BaseSearchRequest {

    @Schema(description = "Tim theo ten dang nhap, ho ten, email hoac so dien thoai")
    private String keyword;

    private UserStatus status;

    @Schema(description = "Loc theo ma nhom quyen", example = "SHIPPER")
    private String roleGroupCode;

    private LocalDate fromDate;

    private LocalDate toDate;
}
