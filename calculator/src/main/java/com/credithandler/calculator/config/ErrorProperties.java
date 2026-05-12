package com.credithandler.calculator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "error")
public class ErrorProperties {

    private Integer businessCode;
    private Integer validationCode;
    private Integer serverCode;
}