package com.viettel.delivery.dto.response.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Schema(name = "RevenueByProvince", description = "Doanh thu theo tinh thanh giao hang")
public class RevenueByProvince implements Serializable {

    private final String province;
    private final long quantity;
    private final BigDecimal revenue;

    public RevenueByProvince(String province, Long quantity, BigDecimal revenue) {
        this.province = province == null ? "Khác" : province;
        this.quantity = quantity == null ? 0 : quantity;
        this.revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }
}
