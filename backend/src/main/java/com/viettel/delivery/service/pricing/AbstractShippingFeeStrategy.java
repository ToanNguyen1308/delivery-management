package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.model.FeeBreakdown;
import com.viettel.delivery.model.FeeCalculationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Phan tinh toan dung chung cho moi loai dich vu:
 * phi = phi co ban + phi quang duong vuot + phi khoi luong vuot + phu phi + phi thu ho COD.
 */
public abstract class AbstractShippingFeeStrategy implements ShippingFeeStrategy {

    private static final BigDecimal FRAGILE_SURCHARGE_PERCENT = new BigDecimal("10");
    private static final BigDecimal PERCENT_BASE = new BigDecimal("100");

    @Override
    public FeeBreakdown calculate(FeeCalculationContext context, PricingRule rule) {
        BigDecimal distanceFee = calculateDistanceFee(context, rule);
        BigDecimal weightFee = calculateWeightFee(context, rule);
        BigDecimal surcharge = calculateSurcharge(context, rule, distanceFee);
        BigDecimal codFee = calculateCodFee(context, rule);

        BigDecimal total = rule.getBaseFee()
                .add(distanceFee)
                .add(weightFee)
                .add(surcharge)
                .add(codFee);

        BigDecimal minFee = rule.getMinFee() == null ? BigDecimal.ZERO : rule.getMinFee();
        if (total.compareTo(minFee) < 0) {
            total = minFee;
        }

        return FeeBreakdown.builder()
                .baseFee(round(rule.getBaseFee()))
                .distanceFee(round(distanceFee))
                .weightFee(round(weightFee))
                .surcharge(round(surcharge))
                .codFee(round(codFee))
                .shippingFee(round(total))
                .build();
    }

    /**
     * Phu phi rieng cua tung loai dich vu, mac dinh chi gom phu phi vung xa va hang de vo.
     */
    protected BigDecimal calculateSurcharge(FeeCalculationContext context, PricingRule rule, BigDecimal distanceFee) {
        BigDecimal surcharge = BigDecimal.ZERO;
        if (context.remoteArea() && rule.getRemoteAreaSurcharge() != null) {
            surcharge = surcharge.add(rule.getRemoteAreaSurcharge());
        }
        if (context.fragile()) {
            surcharge = surcharge.add(percentOf(rule.getBaseFee(), FRAGILE_SURCHARGE_PERCENT));
        }
        return surcharge;
    }

    private BigDecimal calculateDistanceFee(FeeCalculationContext context, PricingRule rule) {
        BigDecimal exceeded = context.distanceKm().subtract(rule.getBaseDistanceKm());
        if (exceeded.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return exceeded.multiply(rule.getPerKmFee());
    }

    private BigDecimal calculateWeightFee(FeeCalculationContext context, PricingRule rule) {
        BigDecimal exceeded = context.weightKg().subtract(rule.getBaseWeightKg());
        if (exceeded.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return exceeded.setScale(0, RoundingMode.CEILING).multiply(rule.getPerKgFee());
    }

    private BigDecimal calculateCodFee(FeeCalculationContext context, PricingRule rule) {
        if (context.codAmount().signum() <= 0 || rule.getCodFeePercent() == null) {
            return BigDecimal.ZERO;
        }
        return percentOf(context.codAmount(), rule.getCodFeePercent());
    }

    protected BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent).divide(PERCENT_BASE, 2, RoundingMode.HALF_UP);
    }

    protected BigDecimal round(BigDecimal value) {
        return value.setScale(AppConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
