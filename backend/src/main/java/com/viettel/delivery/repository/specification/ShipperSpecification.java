package com.viettel.delivery.repository.specification;

import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.dto.search.ShipperSearchRequest;
import com.viettel.delivery.entity.Shipper;
import com.viettel.delivery.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ShipperSpecification {

    private ShipperSpecification() {
    }

    public static Specification<Shipper> build(ShipperSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            // Join fetch user để tránh N+1 khi map DTO (không dùng cho count query)
            Join<Shipper, User> userJoin = root.join("user", JoinType.INNER);
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("user", JoinType.INNER);
            }

            if (StringUtils.hasText(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("shipperCode")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("licensePlate")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("fullName")), pattern),
                        criteriaBuilder.like(userJoin.get("phoneNumber"), pattern)
                ));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (request.getVehicleType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleType"), request.getVehicleType()));
            }

            if (StringUtils.hasText(request.getZone())) {
                predicates.add(criteriaBuilder.equal(root.get("zone"), request.getZone()));
            }

            if (Boolean.TRUE.equals(request.getAvailableOnly())) {
                predicates.add(root.get("status").in(ShipperStatus.ONLINE, ShipperStatus.BUSY));
                predicates.add(criteriaBuilder.lessThan(root.get("currentLoad"), root.get("maxConcurrentOrders")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
