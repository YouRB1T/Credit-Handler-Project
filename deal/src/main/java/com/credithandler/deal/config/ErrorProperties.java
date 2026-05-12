package com.credithandler.deal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "error")
public class ErrorProperties {

    private Integer businessCode;
    private Integer validationCode;
    private Integer serverCode;
}