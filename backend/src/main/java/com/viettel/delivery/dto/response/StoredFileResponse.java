package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@Schema(name = "StoredFileResponse", description = "Thong tin file da upload len MinIO")
public class StoredFileResponse implements Serializable {

    private Long id;

    @Schema(description = "Duong dan doi tuong trong bucket", example = "temp/2026/09/abc.jpg")
    private String objectKey;

    private String bucket;
    private String originalName;
    private String contentType;
    private Long sizeBytes;

    @Schema(description = "URL truy cap file")
    private String url;
}
