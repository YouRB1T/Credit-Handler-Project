package com.credithandler.dossier.exception;

import com.credithandler.api.constants.ErrorMessages;
import com.credithandler.api.dto.error.BusinessErrorResponse;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.dossier.config.ErrorProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BusinessExceptionHandler")
class BusinessExceptionHandlerTest {

    private final ErrorProperties errorProperties = errorProperties();
    private final BusinessExceptionHandler handler = new BusinessExceptionHandler(errorProperties);

    @Test
    void handleBusinessExceptionReturnsConfiguredBusinessResponse() {
        BusinessException exception = BusinessException.of("business failed", "details");

        ResponseEntity<BusinessErrorResponse> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getError()).isEqualTo(ErrorMessages.ERROR_BUSINESS_TITLE);
        assertThat(response.getBody().getMessage()).isEqualTo("business failed");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleValidationExceptionsReturnsFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestRequest(), "request");
        bindingResult.addError(new FieldError("request", "email", "must not be blank"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<BusinessErrorResponse> response = handler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getError()).isEqualTo(ErrorMessages.ERROR_BUSINESS_TITLE);
        assertThat(response.getBody().getMessage()).contains("email", "must not be blank");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    private static ErrorProperties errorProperties() {
        ErrorProperties properties = new ErrorProperties();
        properties.setBusinessCode(422);
        return properties;
    }

    private static final class TestRequest {
    }
}
