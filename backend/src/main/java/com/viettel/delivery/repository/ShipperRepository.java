package com.viettel.delivery.repository;

import com.viettel.delivery.constant.enums.ShipperStatus;
import com.viettel.delivery.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long>, JpaSpecificationExecutor<Shipper> {

    @Query("""
            SELECT s FROM Shipper s
            JOIN FETCH s.user
            WHERE s.id = :id AND s.isDeleted = false
            """)
    Optional<Shipper> findByIdWithUser(@Param("id") Long id);

    @Query("""
            SELECT s FROM Shipper s
            JOIN FETCH s.user u
            WHERE u.id = :userId AND s.isDeleted = false
            """)
    Optional<Shipper> findByUserIdWithUser(@Param("userId") Long userId);

    boolean existsByUserIdAndIsDeletedFalse(Long userId);

    boolean existsByShipperCodeAndIsDeletedFalse(String shipperCode);

    /**
     * Danh sach shipper co the nhan them don, dung cho man hinh dieu phoi va thuat toan gan tu dong.
     */
    @Query("""
            SELECT s FROM Shipper s
            JOIN FETCH s.user
            WHERE s.isDeleted = false
              AND s.status IN :statuses
              AND s.currentLoad < s.maxConcurrentOrders
            ORDER BY s.currentLoad ASC, s.rating DESC
            """)
    List<Shipper> findAvailableShippers(@Param("statuses") Collection<ShipperStatus> statuses);

    @Query("SELECT COALESCE(MAX(s.id), 0) FROM Shipper s")
    long findMaxId();

    @Query("SELECT COUNT(s) FROM Shipper s WHERE s.isDeleted = false AND s.status = :status")
    long countByStatus(@Param("status") ShipperStatus status);
}
