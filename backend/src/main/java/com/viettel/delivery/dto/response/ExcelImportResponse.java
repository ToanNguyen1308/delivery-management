package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@Schema(name = "ExcelImportResponse", description = "Ket qua import du lieu tu Excel")
public class ExcelImportResponse implements Serializable {

    private int totalRows;
    private int successCount;
    private int failedCount;

    @Schema(description = "Ma van don da tao thanh cong")
    private List<String> createdOrderCodes;

    @Schema(description = "Chi tiet cac dong loi")
    private List<RowError> errors;

    @Getter
    @Builder
    @Schema(name = "ExcelRowError", description = "Loi tai mot dong trong file Excel")
    public static class RowError implements Serializable {
        @Schema(description = "So dong tren file Excel, tinh tu 1")
        private int rowNumber;
        private String message;
    }
}
