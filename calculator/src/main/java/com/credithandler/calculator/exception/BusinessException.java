package com.credithandler.calculator.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class BusinessException extends RuntimeException {
    private static final String BUSINESS_EXCEPTION_CODE = "422";

    private final String code;
    private final String message;
    private final String description;
    private final LocalDateTime timestamp;

    private BusinessException(String message, String description) {
        super(message);
        this.code = BUSINESS_EXCEPTION_CODE;
        this.message = message;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public static BusinessException of(String message, String description) {
        return new BusinessException(message, description);
    }
}
