package com.credithandler.calculator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "loan")
public class LoanProperties {
    private BigDecimal baseInterest;

    private BigDecimal insuranceDecrease;

    private BigDecimal salaryDecrease;
}