package com.credithandler.calculator.service;

import com.credithandler.calculator.api.dto.calc.CreditDto;
import com.credithandler.calculator.api.dto.calc.ScoringDataDto;
import com.credithandler.calculator.api.dto.loan.LoanOfferDto;
import com.credithandler.calculator.api.dto.loan.LoanStatementRequestDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculatingOffers(LoanStatementRequestDto request);
    CreditDto calculateCredit(ScoringDataDto request);
}
