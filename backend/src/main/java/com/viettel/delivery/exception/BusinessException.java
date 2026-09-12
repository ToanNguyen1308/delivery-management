package com.viettel.delivery.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception nghiep vu. errorCode chinh la key i18n, args dung de dien tham so vao message.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final transient Object[] args;

    public BusinessException(String errorCode) {
        this(errorCode, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String errorCode, HttpStatus httpStatus) {
        super(errorCode);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = new Object[0];
    }

    public BusinessException(String errorCode, Object... args) {
        super(errorCode);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.args = args;
    }

    public BusinessException(String errorCode, HttpStatus httpStatus, Object... args) {
        super(errorCode);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.args = args;
    }
}
