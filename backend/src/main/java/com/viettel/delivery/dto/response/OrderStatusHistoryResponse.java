package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "OrderStatusHistoryResponse", description = "Mot buoc trong lich su trang thai don hang")
public class OrderStatusHistoryResponse implements Serializable {

    private Long id;
    private EnumResponse fromStatus;
    private EnumResponse toStatus;
    private String note;
    private String changedByName;
    private LocalDateTime changedAt;
}
