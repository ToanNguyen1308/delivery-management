package com.viettel.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(name = "NotificationResponse", description = "Thong bao gui den nguoi dung")
public class NotificationResponse implements Serializable {

    private Long id;
    private EnumResponse type;
    private String title;
    private String content;
    private String referenceType;
    private Long referenceId;
    private String referenceCode;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdDate;
}
