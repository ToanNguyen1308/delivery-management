package com.viettel.delivery.repository.specification;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.dto.search.OrderSearchRequest;
import com.viettel.delivery.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dieu kien tim kiem dong cho don hang, bao gom ca phan quyen du lieu theo nguoi dang nhap.
 */
public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> build(OrderSearchRequest request,
                                             Long restrictCustomerId,
                                             Long restrictShipperId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            // Khách chỉ thấy đơn mình tạo, shipper chỉ thấy đơn được giao
            if (restrictCustomerId != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), restrictCustomerId));
            }
            if (restrictShipperId != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentShipper").get("id"), restrictShipperId));
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("receiverName")), pattern),
                        criteriaBuilder.like(root.get("receiverPhone"), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("deliveryAddress")), pattern)
                ));
            }

            if (!CollectionUtils.isEmpty(request.getStatuses())) {
                predicates.add(root.get("status").in(request.getStatuses()));
            }

            if (request.getPaymentStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), request.getPaymentStatus()));
            }

            if (request.getPaymentMethod() != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), request.getPaymentMethod()));
            }

            if (request.getServiceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), request.getServiceType()));
            }

            if (request.getShipperId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("currentShipper").get("id"), request.getShipperId()));
            }

            if (request.getCustomerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), request.getCustomerId()));
            }

            if (StringUtils.hasText(request.getDeliveryProvince())) {
                predicates.add(criteriaBuilder.equal(root.get("deliveryProvince"), request.getDeliveryProvince()));
            }

            if (StringUtils.hasText(request.getDeliveryDistrict())) {
                predicates.add(criteriaBuilder.equal(root.get("deliveryDistrict"), request.getDeliveryDistrict()));
            }

            if (Boolean.TRUE.equals(request.getUnassignedOnly())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), OrderStatus.CONFIRMED));
                predicates.add(criteriaBuilder.isNull(root.get("currentShipper")));
            }

            if (request.getFromDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"),
                        request.getFromDate().atStartOfDay()));
            }

            if (request.getToDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"),
                        request.getToDate().atTime(LocalTime.MAX)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
