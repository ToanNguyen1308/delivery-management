package com.viettel.delivery.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Chi tiet cac thanh phan cau thanh cuoc van chuyen, giup frontend hien thi minh bach.
 */
@Builder
public record FeeBreakdown(BigDecimal baseFee,
                           BigDecimal distanceFee,
                           BigDecimal weightFee,
                           BigDecimal surcharge,
                           BigDecimal codFee,
                           BigDecimal shippingFee) {
}
