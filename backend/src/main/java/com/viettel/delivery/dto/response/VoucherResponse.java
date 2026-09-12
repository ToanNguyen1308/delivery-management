package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "VoucherResponse", description = "Thong tin ma giam gia")
public class VoucherResponse implements Serializable {

    private Long id;
    private String code;
    private String name;
    private EnumResponse discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer quantity;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private EnumResponse status;
    private String description;
}
