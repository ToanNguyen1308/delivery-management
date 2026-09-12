package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.model.FeeCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Giao trong ngay: don dat sau gio cao diem phai cong them phu phi
 * vi quy thoi gian con lai trong ngay rat ngan.
 */
@Component
public class SameDayShippingFeeStrategy extends AbstractShippingFeeStrategy {

    private static final LocalTime PEAK_HOUR_THRESHOLD = LocalTime.of(15, 0);
    private static final BigDecimal PEAK_HOUR_SURCHARGE_PERCENT = new BigDecimal("20");

    @Override
    public ServiceType getServiceType() {
        return ServiceType.SAME_DAY;
    }

    @Override
    protected BigDecimal calculateSurcharge(FeeCalculationContext context, PricingRule rule, BigDecimal distanceFee) {
        BigDecimal baseSurcharge = super.calculateSurcharge(context, rule, distanceFee);
        if (context.orderTime().toLocalTime().isBefore(PEAK_HOUR_THRESHOLD)) {
            return baseSurcharge;
        }
        BigDecimal chargeableAmount = rule.getBaseFee().add(distanceFee);
        return baseSurcharge.add(percentOf(chargeableAmount, PEAK_HOUR_SURCHARGE_PERCENT));
    }
}
