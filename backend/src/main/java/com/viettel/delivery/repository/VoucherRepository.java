package com.viettel.delivery.repository;

import com.viettel.delivery.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {

    Optional<Voucher> findByCodeAndIsDeletedFalse(String code);

    Optional<Voucher> findByIdAndIsDeletedFalse(Long id);

    boolean existsByCodeAndIsDeletedFalse(String code);
}
