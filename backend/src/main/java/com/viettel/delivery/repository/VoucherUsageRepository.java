package com.viettel.delivery.repository;

import com.viettel.delivery.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Long> {

    @Query("""
            SELECT COUNT(vu) FROM VoucherUsage vu
            WHERE vu.voucher.id = :voucherId AND vu.user.id = :userId AND vu.isDeleted = false
            """)
    long countByVoucherAndUser(@Param("voucherId") Long voucherId, @Param("userId") Long userId);

    @Query("""
            SELECT vu FROM VoucherUsage vu
            JOIN FETCH vu.voucher
            WHERE vu.orderId = :orderId AND vu.isDeleted = false
            """)
    Optional<VoucherUsage> findByOrderId(@Param("orderId") Long orderId);
}
