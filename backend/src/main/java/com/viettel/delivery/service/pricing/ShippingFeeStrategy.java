package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.model.FeeBreakdown;
import com.viettel.delivery.model.FeeCalculationContext;

/**
 * Chien luoc tinh cuoc theo tung loai dich vu (Strategy pattern).
 * Them loai dich vu moi chi can bo sung mot implementation, khong sua code cu.
 */
public interface ShippingFeeStrategy {

    ServiceType getServiceType();

    FeeBreakdown calculate(FeeCalculationContext context, PricingRule rule);
}
