package com.sara.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends RuntimeException {

    private final HttpStatus status;

    public ValidationException(String message) {
        super(message);
        this.status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
    }

    public ValidationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
