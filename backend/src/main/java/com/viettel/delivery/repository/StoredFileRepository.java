package com.viettel.delivery.repository;

import com.viettel.delivery.entity.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    Optional<StoredFile> findByObjectKeyAndIsDeletedFalse(String objectKey);

    List<StoredFile> findAllByFeatureAndReferenceIdAndIsDeletedFalse(String feature, Long referenceId);
}
