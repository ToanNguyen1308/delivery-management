package com.viettel.delivery.dto.response;

import com.viettel.delivery.constant.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Format response thong nhat cho toan bo API: {code, message, data}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "ApiResponse", description = "Cau truc response chung cua he thong")
public class ApiResponse<T> implements Serializable {

    @Schema(description = "Ma ket qua: 'success' hoac ma loi dinh danh", example = "success")
    private String code;

    @Schema(description = "Thong diep da duoc dich theo header Accept-Language", example = "Success")
    private String message;

    @Schema(description = "Du lieu tra ve")
    private T data;

    @Schema(description = "Chi tiet loi validate theo tung truong")
    private Map<String, String> errors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(AppConstants.RESPONSE_CODE_SUCCESS)
                .message(AppConstants.RESPONSE_MESSAGE_SUCCESS)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(AppConstants.RESPONSE_CODE_SUCCESS)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .code(AppConstants.RESPONSE_CODE_SUCCESS)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .errors(errors)
                .build();
    }
}
