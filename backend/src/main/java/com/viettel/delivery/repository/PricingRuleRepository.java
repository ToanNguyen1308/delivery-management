package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.ServiceType;
import com.viettel.delivery.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    Optional<PricingRule> findByServiceTypeAndIsDeletedFalse(ServiceType serviceType);

    Optional<PricingRule> findByIdAndIsDeletedFalse(Long id);

    List<PricingRule> findAllByIsDeletedFalseOrderByServiceTypeAsc();
}
