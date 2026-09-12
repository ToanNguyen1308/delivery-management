package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.OrderStatus;
import com.viettel.delivery.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Nap san quan he ToOne de man hinh danh sach khong sinh truy van N+1.
     */
    @Override
    @EntityGraph(attributePaths = {"customer", "currentShipper", "currentShipper.user"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.customer
            LEFT JOIN FETCH o.currentShipper s
            LEFT JOIN FETCH s.user
            WHERE o.id = :id AND o.isDeleted = false
            """)
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.customer
            LEFT JOIN FETCH o.currentShipper s
            LEFT JOIN FETCH s.user
            WHERE o.orderCode = :orderCode AND o.isDeleted = false
            """)
    Optional<Order> findByOrderCodeWithDetails(@Param("orderCode") String orderCode);

    Optional<Order> findByIdAndIsDeletedFalse(Long id);

    boolean existsByOrderCode(String orderCode);

    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.customer
            WHERE o.status IN :statuses AND o.isDeleted = false
            ORDER BY o.createdDate ASC
            """)
    List<Order> findByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.currentShipper.id = :shipperId
              AND o.status IN :statuses
              AND o.isDeleted = false
            """)
    long countActiveOrdersByShipper(@Param("shipperId") Long shipperId,
                                    @Param("statuses") Collection<OrderStatus> statuses);

    /**
     * Don da qua han giao du kien nhung chua ket thuc, dung cho job nhac nho.
     */
    @Query("""
            SELECT o FROM Order o
            JOIN FETCH o.customer
            WHERE o.expectedDeliveryAt < :now
              AND o.status IN :statuses
              AND o.isDeleted = false
            """)
    List<Order> findOverdueOrders(@Param("now") LocalDateTime now,
                                  @Param("statuses") Collection<OrderStatus> statuses);
}
