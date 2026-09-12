package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.model.FeeCalculationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Giao nhanh: ngoai phu phi chung con cong them phi uu tien tuyen rieng
 * tinh tren phan phi quang duong.
 */
@Component
public class ExpressShippingFeeStrategy extends AbstractShippingFeeStrategy {

    private static final BigDecimal PRIORITY_SURCHARGE_PERCENT = new BigDecimal("15");

    @Override
    public ServiceType getServiceType() {
        return ServiceType.EXPRESS;
    }

    @Override
    protected BigDecimal calculateSurcharge(FeeCalculationContext context, PricingRule rule, BigDecimal distanceFee) {
        BigDecimal baseSurcharge = super.calculateSurcharge(context, rule, distanceFee);
        return baseSurcharge.add(percentOf(distanceFee, PRIORITY_SURCHARGE_PERCENT));
    }
}
