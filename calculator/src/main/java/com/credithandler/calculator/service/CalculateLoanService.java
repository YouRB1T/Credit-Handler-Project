package com.credithandler.calculator.service;

import com.credithandler.calculator.dto.loan.LoanOfferDto;

import java.math.BigDecimal;

public interface CalculateLoanService {
    LoanOfferDto calculateLoan(BigDecimal amount, Integer term, Boolean isInsuranceEnabled, Boolean isSalaryClient);
    //BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, Boolean isInsuranceEnabled, Boolean isSalaryClient);
    //BigDecimal calculateRate(Boolean isInsuranceEnabled, Boolean isSalaryClient);
    //BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term);
}
