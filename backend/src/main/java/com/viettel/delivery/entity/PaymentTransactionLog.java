package com.viettel.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Luu nguyen ban du lieu trao doi voi cong thanh toan de doi soat khi co tranh chap.
 */
@Entity
@Table(name = "payment_transaction_logs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    /**
     * Dung LONGVARCHAR thay vi @Lob de Hibernate anh xa sang TEXT tren PostgreSQL
     * va LONGTEXT tren MariaDB, dung voi kieu CLOB ma Liquibase tao ra.
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "request_data")
    private String requestData;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "response_data")
    private String responseData;

    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Column(name = "message", length = 500)
    private String message;
}
