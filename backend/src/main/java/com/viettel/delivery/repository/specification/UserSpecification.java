package com.viettel.delivery.repository.specification;

import com.viettel.delivery.dto.search.UserSearchRequest;
import com.viettel.delivery.entity.RoleGroup;
import com.viettel.delivery.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dieu kien tim kiem dong cho nguoi dung. Moi dieu kien chi duoc them khi request co gia tri,
 * tranh phai tao hang loat method findByAAndBAndC trong repository.
 */
public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> build(UserSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (StringUtils.hasText(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern),
                        criteriaBuilder.like(root.get("phoneNumber"), pattern)
                ));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (StringUtils.hasText(request.getRoleGroupCode())) {
                Join<User, RoleGroup> roleGroupJoin = root.join("roleGroups");
                predicates.add(criteriaBuilder.equal(roleGroupJoin.get("roleGroupCode"), request.getRoleGroupCode()));
                if (query != null) {
                    query.distinct(true);
                }
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
