package com.viettel.delivery.service;

import com.viettel.delivery.dto.response.StoredFileResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Truu tuong hoa noi luu tru file. Hien tai dung MinIO, co the thay bang S3/OSS
 * ma khong anh huong den tang service nghiep vu.
 */
public interface FileStorageService {

    /**
     * Upload vao thu muc temp truoc khi nghiep vu xac nhan.
     */
    StoredFileResponse uploadToTemp(MultipartFile file);

    /**
     * Chuyen file tu temp sang thu muc dich theo dang feature/yyyy/MM/ten-file.
     */
    StoredFileResponse moveToFeature(String tempObjectKey, String feature, Long referenceId);

    /**
     * Upload thang vao thu muc dich, dung cho anh chup truc tiep tren app shipper.
     */
    StoredFileResponse upload(MultipartFile file, String feature, Long referenceId);

    void delete(String objectKey);

    String buildPublicUrl(String objectKey);
}
