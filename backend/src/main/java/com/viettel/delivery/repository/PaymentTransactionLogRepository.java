package com.viettel.delivery.repository;

import com.viettel.delivery.entity.PaymentTransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionLogRepository extends JpaRepository<PaymentTransactionLog, Long> {

    List<PaymentTransactionLog> findAllByPaymentIdAndIsDeletedFalseOrderByCreatedDateAsc(Long paymentId);
}
