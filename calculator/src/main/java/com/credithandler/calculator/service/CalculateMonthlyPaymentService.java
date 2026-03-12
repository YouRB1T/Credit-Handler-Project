package com.credithandler.calculator.service;

import java.math.BigDecimal;

public interface CalculateMonthlyPaymentService {
    BigDecimal monthlyPayment(BigDecimal amount, BigDecimal annualRate, Integer term);
}
