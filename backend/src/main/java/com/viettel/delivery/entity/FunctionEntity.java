package com.viettel.delivery.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Chuc nang he thong (function_code) dung cho phan quyen @PreAuthorize.
 */
@Entity
@Table(name = "functions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionEntity extends BaseEntity {

    @Column(name = "function_code", nullable = false, length = 100, unique = true)
    private String functionCode;

    @Column(name = "function_name", nullable = false, length = 150)
    private String functionName;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "description")
    private String description;
}
