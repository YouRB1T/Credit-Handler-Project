package com.credithandler.dossier.exception;

import com.credithandler.api.constants.ErrorMessages;
import com.credithandler.api.dto.error.BusinessErrorResponse;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.dossier.config.ErrorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        BusinessErrorResponse errorResponse = new BusinessErrorResponse();
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setStatus(errorProperties.getBusinessCode());
        errorResponse.setError(ErrorMessages.ERROR_BUSINESS_TITLE);
        errorResponse.setMessage(errors.toString());

        return ResponseEntity
                .status(errorProperties.getBusinessCode())
                .body(errorResponse);
    }
}
