package com.viettel.delivery.dto.response.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Schema(name = "OrderCountByDate", description = "So don va doanh thu theo tung ngay")
public class OrderCountByDate implements Serializable {

    private final LocalDate date;
    private final long quantity;
    private final BigDecimal revenue;

    /**
     * Nhan rieng nam, thang, ngay de cau truy van dung ham JPQL chuan, khong phu thuoc DBMS.
     */
    public OrderCountByDate(Integer year, Integer month, Integer day, Long quantity, BigDecimal revenue) {
        this.date = LocalDate.of(year, month, day);
        this.quantity = quantity == null ? 0 : quantity;
        this.revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }
}
