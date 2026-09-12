package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@Schema(name = "DashboardOverviewResponse", description = "Cac chi so tong quan tren dashboard")
public class DashboardOverviewResponse implements Serializable {

    private long totalOrders;
    private long ordersToday;
    private long deliveredOrders;
    private long inProgressOrders;
    private long cancelledOrders;

    @Schema(description = "Ti le giao thanh cong tren tong don da ket thuc (%)")
    private BigDecimal successRate;

    @Schema(description = "Tong doanh thu cuoc van chuyen tu cac don da giao")
    private BigDecimal totalRevenue;

    private BigDecimal revenueToday;

    @Schema(description = "Tong tien COD da thu ho")
    private BigDecimal totalCodCollected;

    private long totalShippers;
    private long onlineShippers;

    @Schema(description = "Thoi gian giao hang trung binh (gio)")
    private BigDecimal averageDeliveryHours;
}
