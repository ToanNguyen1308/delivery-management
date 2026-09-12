package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.CodSettlementStatus;
import com.viettel.delivery.entity.CodSettlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodSettlementRepository extends JpaRepository<CodSettlement, Long> {

    boolean existsByOrderIdAndIsDeletedFalse(Long orderId);

    @Query("""
            SELECT c FROM CodSettlement c
            JOIN FETCH c.order o
            JOIN FETCH c.shipper s
            JOIN FETCH s.user
            WHERE c.id = :id AND c.isDeleted = false
            """)
    Optional<CodSettlement> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            SELECT c FROM CodSettlement c
            WHERE c.shipper.id = :shipperId AND c.status = :status AND c.isDeleted = false
            """)
    List<CodSettlement> findByShipperAndStatus(@Param("shipperId") Long shipperId,
                                               @Param("status") CodSettlementStatus status);

    @Query("""
            SELECT COALESCE(SUM(c.amount), 0) FROM CodSettlement c
            WHERE c.shipper.id = :shipperId AND c.status = :status AND c.isDeleted = false
            """)
    BigDecimal sumAmountByShipperAndStatus(@Param("shipperId") Long shipperId,
                                           @Param("status") CodSettlementStatus status);

    @Query(value = """
            SELECT c FROM CodSettlement c
            JOIN FETCH c.order o
            JOIN FETCH c.shipper s
            JOIN FETCH s.user
            WHERE c.status IN :statuses AND c.isDeleted = false
            ORDER BY c.createdDate DESC
            """,
            countQuery = """
                    SELECT COUNT(c) FROM CodSettlement c
                    WHERE c.status IN :statuses AND c.isDeleted = false
                    """)
    Page<CodSettlement> findByStatuses(@Param("statuses") Collection<CodSettlementStatus> statuses,
                                       Pageable pageable);

    @Query(value = """
            SELECT c FROM CodSettlement c
            JOIN FETCH c.order o
            JOIN FETCH c.shipper s
            JOIN FETCH s.user
            WHERE c.shipper.id = :shipperId AND c.status IN :statuses AND c.isDeleted = false
            ORDER BY c.createdDate DESC
            """,
            countQuery = """
                    SELECT COUNT(c) FROM CodSettlement c
                    WHERE c.shipper.id = :shipperId AND c.status IN :statuses AND c.isDeleted = false
                    """)
    Page<CodSettlement> findByShipperAndStatuses(@Param("shipperId") Long shipperId,
                                                 @Param("statuses") Collection<CodSettlementStatus> statuses,
                                                 Pageable pageable);
}
