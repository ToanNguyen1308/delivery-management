package com.viettel.delivery.entity;

import com.viettel.delivery.constant.enums.PaymentMethod;
import com.viettel.delivery.constant.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

    /**
     * Ma giao dich gui sang cong thanh toan (vnp_TxnRef), duy nhat trong he thong.
     */
    @Column(name = "txn_ref", nullable = false, length = 50, unique = true)
    private String txnRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private PaymentMethod provider;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "pay_url", length = 2000)
    private String payUrl;

    @Column(name = "transaction_no", length = 100)
    private String transactionNo;

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "card_type", length = 50)
    private String cardType;

    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Column(name = "pay_date", length = 20)
    private String payDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "order_info")
    private String orderInfo;

    public boolean isFinalized() {
        return PaymentStatus.PAID.equals(status) || PaymentStatus.FAILED.equals(status);
    }
}
