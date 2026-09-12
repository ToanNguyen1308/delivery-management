package com.viettel.delivery.repository.specification;

import com.viettel.delivery.constant.enums.VoucherStatus;
import com.viettel.delivery.dto.search.VoucherSearchRequest;
import com.viettel.delivery.entity.Voucher;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class VoucherSpecification {

    private VoucherSpecification() {
    }

    public static Specification<Voucher> build(VoucherSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (StringUtils.hasText(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern)
                ));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getDiscountType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("discountType"), request.getDiscountType()));
            }

            if (Boolean.TRUE.equals(request.getAvailableOnly())) {
                LocalDateTime now = LocalDateTime.now();
                predicates.add(criteriaBuilder.equal(root.get("status"), VoucherStatus.ACTIVE));
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("validFrom"), now));
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("validTo"), now));
                predicates.add(criteriaBuilder.lessThan(root.get("usedCount"), root.get("quantity")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
