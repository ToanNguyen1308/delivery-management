package com.viettel.delivery.dto.response.statistic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Schema(name = "TopShipperStatistic", description = "Shipper co so don giao thanh cong cao nhat")
public class TopShipperStatistic implements Serializable {

    private final Long shipperId;
    private final String shipperCode;
    private final String fullName;
    private final long deliveredCount;
    private final BigDecimal rating;

    public TopShipperStatistic(Long shipperId, String shipperCode, String fullName,
                               Long deliveredCount, BigDecimal rating) {
        this.shipperId = shipperId;
        this.shipperCode = shipperCode;
        this.fullName = fullName;
        this.deliveredCount = deliveredCount == null ? 0 : deliveredCount;
        this.rating = rating == null ? BigDecimal.ZERO : rating;
    }
}
