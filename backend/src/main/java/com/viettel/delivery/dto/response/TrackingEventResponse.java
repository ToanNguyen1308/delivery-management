package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "TrackingEventResponse", description = "Mot su kien tren hanh trinh don hang")
public class TrackingEventResponse implements Serializable {

    private Long id;
    private EnumResponse eventType;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;
    private LocalDateTime occurredAt;
}
