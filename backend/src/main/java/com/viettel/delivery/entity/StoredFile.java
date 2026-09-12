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
 * Metadata cua file luu tren MinIO. Noi dung file khong bao gio luu trong database.
 */
@Entity
@Table(name = "stored_files")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFile extends BaseEntity {

    @Column(name = "object_key", nullable = false, length = 500)
    private String objectKey;

    @Column(name = "bucket", nullable = false, length = 100)
    private String bucket;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "feature", length = 50)
    private String feature;

    @Column(name = "reference_id")
    private Long referenceId;
}
