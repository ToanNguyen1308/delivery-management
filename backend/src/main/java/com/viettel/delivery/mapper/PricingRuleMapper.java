package com.viettel.delivery.mapper;

import com.viettel.delivery.dto.request.PricingRuleRequest;
import com.viettel.delivery.dto.response.PricingRuleResponse;
import com.viettel.delivery.entity.PricingRule;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = EnumMapper.class)
public interface PricingRuleMapper {

    PricingRuleResponse toResponse(PricingRule pricingRule);

    List<PricingRuleResponse> toResponseList(List<PricingRule> pricingRules);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serviceType", ignore = true)
    void updateEntity(@MappingTarget PricingRule pricingRule, PricingRuleRequest request);
}
