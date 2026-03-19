package com.credithandler.calculator.exception;

import com.credithandler.calculator.config.ErrorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class BusinessExceptionHandler {
    private final ErrorProperties errorProperties;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(errorProperties.getResponseTimestamp(), LocalDateTime.now());
        errorResponse.put(errorProperties.getResponseStatus(), errorProperties.getBusinessCode());
        errorResponse.put(errorProperties.getResponseError(), errorProperties.getBusinessErrorTitle());
        errorResponse.put(errorProperties.getResponseMessage(), ex.getMessage());

        return ResponseEntity
                .status(errorProperties.getBusinessCode())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put(errorProperties.getResponseTimestamp(), LocalDateTime.now());
        errorResponse.put(errorProperties.getResponseStatus(), HttpStatus.BAD_REQUEST.value());
        errorResponse.put(errorProperties.getResponseError(), errorProperties.getValidationErrorTitle());
        errorResponse.put(errorProperties.getResponseErrors(), errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

}