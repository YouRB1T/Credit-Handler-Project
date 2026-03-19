package com.credithandler.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "error")
public class ErrorProperties {
    private Integer businessCode;

    private String responseTimestamp;
    private String responseStatus;
    private String responseError;
    private String responseMessage;
    private String responseField;
    private String responseErrors;

    private String businessErrorTitle;
    private String validationErrorTitle;
    private String serverErrorTitle;
    private String serverErrorMessage;
}
