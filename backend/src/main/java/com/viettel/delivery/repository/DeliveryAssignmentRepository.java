package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.AssignmentStatus;
import com.viettel.delivery.entity.DeliveryAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, Long> {

    @Query("""
            SELECT a FROM DeliveryAssignment a
            JOIN FETCH a.order o
            JOIN FETCH a.shipper s
            JOIN FETCH s.user
            WHERE a.id = :id AND a.isDeleted = false
            """)
    Optional<DeliveryAssignment> findByIdWithDetails(@Param("id") Long id);

    /**
     * Danh sach nhiem vu cua mot shipper. Service luon truyen day du tap trang thai can loc
     * nen khong dung dieu kien null-check trong JPQL.
     */
    @Query(value = """
            SELECT a FROM DeliveryAssignment a
            JOIN FETCH a.order o
            JOIN FETCH o.customer
            JOIN FETCH a.shipper s
            JOIN FETCH s.user
            WHERE s.id = :shipperId
              AND a.status IN :statuses
              AND a.isDeleted = false
            ORDER BY a.assignedAt DESC
            """,
            countQuery = """
                    SELECT COUNT(a) FROM DeliveryAssignment a
                    WHERE a.shipper.id = :shipperId
                      AND a.status IN :statuses
                      AND a.isDeleted = false
                    """)
    Page<DeliveryAssignment> findByShipper(@Param("shipperId") Long shipperId,
                                           @Param("statuses") Collection<AssignmentStatus> statuses,
                                           Pageable pageable);

    @Query("""
            SELECT a FROM DeliveryAssignment a
            JOIN FETCH a.shipper s
            JOIN FETCH s.user
            WHERE a.order.id = :orderId AND a.isDeleted = false
            ORDER BY a.assignedAt DESC
            """)
    List<DeliveryAssignment> findByOrderId(@Param("orderId") Long orderId);

    @Query("""
            SELECT a FROM DeliveryAssignment a
            WHERE a.order.id = :orderId AND a.status IN :statuses AND a.isDeleted = false
            """)
    List<DeliveryAssignment> findByOrderIdAndStatuses(@Param("orderId") Long orderId,
                                                      @Param("statuses") Collection<AssignmentStatus> statuses);

    @Query("""
            SELECT COUNT(a) FROM DeliveryAssignment a
            WHERE a.shipper.id = :shipperId AND a.status = :status AND a.isDeleted = false
            """)
    long countByShipperAndStatus(@Param("shipperId") Long shipperId, @Param("status") AssignmentStatus status);
}
