package com.credithandler.calculator.service;

import java.math.BigDecimal;

public interface CalculateMonthlyPayment {
    BigDecimal monthlyPayment(BigDecimal amount, BigDecimal annualRate, Integer term);
}
