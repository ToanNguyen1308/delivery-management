package com.viettel.delivery.dto.response;

import com.viettel.delivery.constant.enums.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Tra enum kem description theo convention (khong tra ve chuoi tran).
 */
@Getter
@AllArgsConstructor
@Schema(name = "EnumResponse", description = "Gia tri enum kem mo ta")
public class EnumResponse implements Serializable {

    private final String code;
    private final String description;

    public static EnumResponse of(BaseEnum value) {
        return value == null ? null : new EnumResponse(value.name(), value.getDescription());
    }

    public static <E extends Enum<E> & BaseEnum> List<EnumResponse> listOf(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(EnumResponse::of)
                .toList();
    }
}
