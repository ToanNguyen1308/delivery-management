package com.viettel.delivery.service;

import com.viettel.delivery.dto.request.FeePreviewRequest;
import com.viettel.delivery.dto.request.PricingRuleRequest;
import com.viettel.delivery.dto.response.FeePreviewResponse;
import com.viettel.delivery.dto.response.PricingRuleResponse;

import java.util.List;

public interface PricingService {

    FeePreviewResponse preview(FeePreviewRequest request);

    List<PricingRuleResponse> getAllRules();

    PricingRuleResponse updateRule(Long id, PricingRuleRequest request);
}
