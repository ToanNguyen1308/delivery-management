package com.viettel.delivery.service.pricing;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.model.FeeBreakdown;
import com.viettel.delivery.model.FeeCalculationContext;
import com.viettel.delivery.repository.PricingRuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Chon chien luoc tinh cuoc phu hop voi loai dich vu (Factory + Strategy).
 */
@Slf4j
@Component
public class ShippingFeeCalculator {

    private final Map<ServiceType, ShippingFeeStrategy> strategies = new EnumMap<>(ServiceType.class);
    private final PricingRuleRepository pricingRuleRepository;

    public ShippingFeeCalculator(List<ShippingFeeStrategy> strategyList,
                                 PricingRuleRepository pricingRuleRepository) {
        strategyList.forEach(strategy -> strategies.put(strategy.getServiceType(), strategy));
        this.pricingRuleRepository = pricingRuleRepository;
        log.info("Da nap {} chien luoc tinh cuoc: {}", strategies.size(), strategies.keySet());
    }

    public FeeBreakdown calculate(ServiceType serviceType, FeeCalculationContext context) {
        ShippingFeeStrategy strategy = strategies.get(serviceType);
        if (strategy == null) {
            throw new ResourceNotFoundException(ErrorCode.PRICING_RULE_NOT_FOUND);
        }
        return strategy.calculate(context, findRule(serviceType));
    }

    @Cacheable(value = AppConstants.CACHE_PRICING, key = "#serviceType.name()")
    public PricingRule findRule(ServiceType serviceType) {
        return pricingRuleRepository.findByServiceTypeAndIsDeletedFalse(serviceType)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRICING_RULE_NOT_FOUND));
    }
}
