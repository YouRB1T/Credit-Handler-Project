package com.credithandler.calculator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
public class LoanProperties {

    @Value("${loan.base-interest}")
    private BigDecimal baseInterest;

    @Value("${loan.insurance-decrease}")
    private BigDecimal insuranceDecrease;

    @Value("${loan.salary-decrease}")
    private BigDecimal salaryDecrease;
}