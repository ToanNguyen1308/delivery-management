package com.viettel.delivery.service.impl;

import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.dto.request.FeePreviewRequest;
import com.viettel.delivery.dto.request.PricingRuleRequest;
import com.viettel.delivery.dto.response.EnumResponse;
import com.viettel.delivery.dto.response.FeePreviewResponse;
import com.viettel.delivery.dto.response.PricingRuleResponse;
import com.viettel.delivery.entity.PricingRule;
import com.viettel.delivery.exception.ResourceNotFoundException;
import com.viettel.delivery.mapper.PricingRuleMapper;
import com.viettel.delivery.model.FeeBreakdown;
import com.viettel.delivery.model.FeeCalculationContext;
import com.viettel.delivery.model.VoucherDiscountResult;
import com.viettel.delivery.repository.PricingRuleRepository;
import com.viettel.delivery.security.SecurityUtil;
import com.viettel.delivery.service.PricingService;
import com.viettel.delivery.service.VoucherService;
import com.viettel.delivery.service.pricing.ShippingFeeCalculator;
import com.viettel.delivery.util.GeoUtil;
import com.viettel.delivery.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final PricingRuleMapper pricingRuleMapper;
    private final ShippingFeeCalculator shippingFeeCalculator;
    private final VoucherService voucherService;
    private final MessageUtil messageUtil;

    @Override
    @Transactional(readOnly = true)
    public FeePreviewResponse preview(FeePreviewRequest request) {
        BigDecimal distanceKm = resolveDistance(request);

        FeeBreakdown breakdown = shippingFeeCalculator.calculate(request.getServiceType(),
                new FeeCalculationContext(distanceKm,
                        request.getWeightKg(),
                        request.getCodAmount(),
                        Boolean.TRUE.equals(request.getFragile()),
                        Boolean.TRUE.equals(request.getRemoteArea()),
                        LocalDateTime.now()));

        VoucherDiscountResult voucherResult = voucherService.evaluate(request.getVoucherCode(),
                breakdown.shippingFee(), SecurityUtil.getCurrentUserIdOptional().orElse(null));

        BigDecimal totalAmount = breakdown.shippingFee().subtract(voucherResult.discountAmount());

        return FeePreviewResponse.builder()
                .serviceType(EnumResponse.of(request.getServiceType()))
                .distanceKm(distanceKm)
                .baseFee(breakdown.baseFee())
                .distanceFee(breakdown.distanceFee())
                .weightFee(breakdown.weightFee())
                .surcharge(breakdown.surcharge())
                .codFee(breakdown.codFee())
                .shippingFee(breakdown.shippingFee())
                .discountAmount(voucherResult.discountAmount())
                .totalAmount(totalAmount.max(BigDecimal.ZERO))
                .voucherCode(request.getVoucherCode())
                .voucherApplied(voucherResult.applied())
                .voucherMessage(voucherResult.messageCode() == null
                        ? null
                        : messageUtil.get(voucherResult.messageCode()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRuleResponse> getAllRules() {
        return pricingRuleMapper.toResponseList(pricingRuleRepository.findAllByIsDeletedFalseOrderByServiceTypeAsc());
    }

    @Override
    @Transactional
    @CacheEvict(value = AppConstants.CACHE_PRICING, allEntries = true)
    public PricingRuleResponse updateRule(Long id, PricingRuleRequest request) {
        PricingRule rule = pricingRuleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRICING_RULE_NOT_FOUND));
        pricingRuleMapper.updateEntity(rule, request);
        return pricingRuleMapper.toResponse(rule);
    }

    /**
     * Uu tien quang duong do client gui len, neu khong co thi tinh tu toa do hai diem.
     */
    private BigDecimal resolveDistance(FeePreviewRequest request) {
        if (request.getDistanceKm() != null && request.getDistanceKm().signum() > 0) {
            return request.getDistanceKm();
        }
        return GeoUtil.roadDistanceKm(request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDeliveryLatitude(), request.getDeliveryLongitude());
    }
}
