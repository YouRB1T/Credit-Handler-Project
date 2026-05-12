package com.credithandler.statement.exception;

import com.credithandler.api.constants.ErrorMessages;
import com.credithandler.api.dto.error.BusinessErrorResponse;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.statement.config.ErrorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class BusinessExceptionHandler {

    private final ErrorProperties errorProperties;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BusinessErrorResponse> handleBusinessException(BusinessException ex) {

        BusinessErrorResponse errorResponse = new BusinessErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(errorProperties.getBusinessCode());
        errorResponse.setError(ErrorMessages.ERROR_BUSINESS_TITLE);
        errorResponse.setMessage(ex.getMessage());

        return ResponseEntity
                .status(errorProperties.getBusinessCode())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BusinessErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));

        BusinessErrorResponse errorResponse = new BusinessErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(errorProperties.getBusinessCode());
        errorResponse.setError(ErrorMessages.ERROR_BUSINESS_TITLE);
        errorResponse.setMessage(errorMessage);

        return ResponseEntity
                .status(errorProperties.getBusinessCode())
                .body(errorResponse);
    }

}