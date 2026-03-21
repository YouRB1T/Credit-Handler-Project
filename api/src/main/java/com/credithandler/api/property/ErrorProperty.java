package com.credithandler.api.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@ConfigurationProperties(prefix = "error")
public class ErrorProperty {
    private Integer businessCode;
    private Integer validationCode;
}
