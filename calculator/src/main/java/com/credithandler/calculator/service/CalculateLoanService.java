package com.credithandler.calculator.service;

import com.credithandler.api.controller.calculator.dto.loan.LoanOfferDto;

import java.math.BigDecimal;

public interface CalculateLoanService {
    LoanOfferDto calculateLoan(BigDecimal amount, Integer term, Boolean isInsuranceEnabled, Boolean isSalaryClient);
}
