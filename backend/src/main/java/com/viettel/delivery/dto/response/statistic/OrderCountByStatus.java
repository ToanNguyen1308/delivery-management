package com.viettel.delivery.dto.response.statistic;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.dto.response.EnumResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;

/**
 * DTO Projection nhan truc tiep ket qua JPQL, chi select dung truong can thiet.
 */
@Getter
@Schema(name = "OrderCountByStatus", description = "So luong don theo tung trang thai")
public class OrderCountByStatus implements Serializable {

    private final EnumResponse status;
    private final long quantity;

    public OrderCountByStatus(OrderStatus status, Long quantity) {
        this.status = EnumResponse.of(status);
        this.quantity = quantity == null ? 0 : quantity;
    }
}
