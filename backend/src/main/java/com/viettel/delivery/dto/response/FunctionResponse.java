package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Schema(name = "FunctionResponse", description = "Chuc nang he thong")
public class FunctionResponse implements Serializable {

    private Long id;
    private String functionCode;
    private String functionName;
    private String module;
    private String description;
}
