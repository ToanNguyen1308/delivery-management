package com.viettel.delivery.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String errorCode, Object... args) {
        super(errorCode, HttpStatus.NOT_FOUND, args);
    }
}
