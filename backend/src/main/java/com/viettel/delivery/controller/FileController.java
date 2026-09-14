package com.viettel.delivery.controller;

import com.viettel.delivery.dto.response.ApiResponse;
import com.viettel.delivery.dto.response.StoredFileResponse;
import com.viettel.delivery.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "11. File", description = "Upload file lên MinIO")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload-temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload file vào thư mục tạm")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadTemp(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(fileStorageService.uploadToTemp(file)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload vào thư mục nghiệp vụ (delivery-proof, avatar, ...)")
    public ResponseEntity<ApiResponse<StoredFileResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam("feature") String feature,
            @RequestParam(value = "referenceId", required = false) Long referenceId) {
        return ResponseEntity.ok(ApiResponse.success(fileStorageService.upload(file, feature, referenceId)));
    }
}
