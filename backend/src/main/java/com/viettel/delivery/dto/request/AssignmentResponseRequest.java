package com.viettel.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "AssignmentResponseRequest", description = "Shipper phan hoi phan cong")
public class AssignmentResponseRequest implements Serializable {

    @NotNull(message = "Phải chọn nhận hoặc từ chối")
    @Schema(description = "true = nhan don, false = tu choi")
    private Boolean accepted;

    @Size(max = 500)
    @Schema(description = "Ly do khi tu choi")
    private String rejectReason;
}
