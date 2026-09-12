package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.PaymentStatus;
import com.viettel.delivery.entity.Payment;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
            SELECT p FROM Payment p
            JOIN FETCH p.order o
            JOIN FETCH o.customer
            WHERE p.txnRef = :txnRef AND p.isDeleted = false
            """)
    Optional<Payment> findByTxnRefWithOrder(@Param("txnRef") String txnRef);

    @Query("""
            SELECT p FROM Payment p
            JOIN FETCH p.order
            WHERE p.order.id = :orderId AND p.isDeleted = false
            ORDER BY p.createdDate DESC
            """)
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    @Query("""
            SELECT p FROM Payment p
            JOIN FETCH p.order o
            JOIN FETCH o.customer
            WHERE p.order.id = :orderId AND p.status IN :statuses AND p.isDeleted = false
            ORDER BY p.createdDate DESC
            """)
    List<Payment> findByOrderIdAndStatuses(@Param("orderId") Long orderId,
                                           @Param("statuses") Collection<PaymentStatus> statuses);

    @Query(value = """
            SELECT p FROM Payment p
            JOIN FETCH p.order o
            JOIN FETCH o.customer
            WHERE p.isDeleted = false
            ORDER BY p.createdDate DESC
            """,
            countQuery = "SELECT COUNT(p) FROM Payment p WHERE p.isDeleted = false")
    Page<Payment> findAllWithOrder(Pageable pageable);

    @Query(value = """
            SELECT p FROM Payment p
            JOIN FETCH p.order o
            JOIN FETCH o.customer c
            WHERE c.id = :customerId AND p.isDeleted = false
            ORDER BY p.createdDate DESC
            """,
            countQuery = """
                    SELECT COUNT(p) FROM Payment p
                    WHERE p.order.customer.id = :customerId AND p.isDeleted = false
                    """)
    Page<Payment> findAllByCustomer(@Param("customerId") Long customerId, Pageable pageable);
}
