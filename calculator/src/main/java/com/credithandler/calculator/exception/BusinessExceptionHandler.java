package com.credithandler.calculator.exception;

import com.credithandler.api.constants.ErrorMessages;
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
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_TIMESTAMP, LocalDateTime.now());
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_STATUS, errorProperties.getBusinessCode());
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_ERROR, ErrorMessages.ERROR_BUSINESS_TITLE);
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_MESSAGE, ex.getMessage());

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
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_TIMESTAMP, LocalDateTime.now());
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_STATUS, errorProperties.getValidationCode());
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_ERROR, ErrorMessages.ERROR_VALIDATION_TITLE);
        errorResponse.put(ErrorMessages.ERROR_RESPONSE_ERRORS, errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

}