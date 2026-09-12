package com.viettel.delivery.service.impl;

import com.viettel.delivery.config.properties.MinioProperties;
import com.viettel.delivery.constant.AppConstants;
import com.viettel.delivery.constant.ErrorCode;
import com.viettel.delivery.dto.response.StoredFileResponse;
import com.viettel.delivery.entity.StoredFile;
import com.viettel.delivery.exception.BusinessException;
import com.viettel.delivery.repository.StoredFileRepository;
import com.viettel.delivery.service.FileStorageService;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Luu file len MinIO theo cau truc thu muc feature/yyyy/MM/, database chi giu metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {

    private static final DateTimeFormatter FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "webp", "pdf", "xlsx", "xls");

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final StoredFileRepository storedFileRepository;

    @Override
    @Transactional
    public StoredFileResponse uploadToTemp(MultipartFile file) {
        return store(file, AppConstants.MINIO_TEMP_FOLDER, null);
    }

    @Override
    @Transactional
    public StoredFileResponse upload(MultipartFile file, String feature, Long referenceId) {
        return store(file, feature, referenceId);
    }

    @Override
    @Transactional
    public StoredFileResponse moveToFeature(String tempObjectKey, String feature, Long referenceId) {
        StoredFile storedFile = storedFileRepository.findByObjectKeyAndIsDeletedFalse(tempObjectKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        String targetKey = buildObjectKey(feature, extractExtension(storedFile.getOriginalName()));
        try {
            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(targetKey)
                    .source(CopySource.builder()
                            .bucket(minioProperties.bucket())
                            .object(tempObjectKey)
                            .build())
                    .build());
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(tempObjectKey)
                    .build());
        } catch (Exception ex) {
            log.error("Khong the chuyen file {} sang thu muc {}", tempObjectKey, feature, ex);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        storedFile.setObjectKey(targetKey);
        storedFile.setUrl(buildPublicUrl(targetKey));
        storedFile.setFeature(feature);
        storedFile.setReferenceId(referenceId);
        return toResponse(storedFile);
    }

    @Override
    @Transactional
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.warn("Khong the xoa file {} tren MinIO: {}", objectKey, ex.getMessage());
        }
        storedFileRepository.findByObjectKeyAndIsDeletedFalse(objectKey)
                .ifPresent(StoredFile::markDeleted);
    }

    @Override
    public String buildPublicUrl(String objectKey) {
        return "%s/%s/%s".formatted(minioProperties.publicEndpoint(), minioProperties.bucket(), objectKey);
    }

    private StoredFileResponse store(MultipartFile file, String feature, Long referenceId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String objectKey = buildObjectKey(feature, extension);
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.bucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (IOException ex) {
            log.error("Khong doc duoc noi dung file {}", file.getOriginalFilename(), ex);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (Exception ex) {
            log.error("Upload file len MinIO that bai", ex);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        StoredFile storedFile = storedFileRepository.save(StoredFile.builder()
                .objectKey(objectKey)
                .bucket(minioProperties.bucket())
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .url(buildPublicUrl(objectKey))
                .feature(feature)
                .referenceId(referenceId)
                .build());

        log.info("Da upload file {} len MinIO", objectKey);
        return toResponse(storedFile);
    }

    private String buildObjectKey(String feature, String extension) {
        return "%s/%s/%s.%s".formatted(feature, LocalDate.now().format(FOLDER_FORMAT),
                UUID.randomUUID().toString().replace("-", ""), extension);
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private StoredFileResponse toResponse(StoredFile storedFile) {
        return StoredFileResponse.builder()
                .id(storedFile.getId())
                .objectKey(storedFile.getObjectKey())
                .bucket(storedFile.getBucket())
                .originalName(storedFile.getOriginalName())
                .contentType(storedFile.getContentType())
                .sizeBytes(storedFile.getSizeBytes())
                .url(storedFile.getUrl())
                .build();
    }
}
