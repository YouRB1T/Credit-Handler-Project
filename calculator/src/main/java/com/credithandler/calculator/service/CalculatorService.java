package com.credithandler.calculator.service;

import com.credithandler.api.controller.calculator.dto.calc.CreditDto;
import com.credithandler.api.controller.calculator.dto.calc.ScoringDataDto;
import com.credithandler.api.controller.calculator.dto.loan.LoanOfferDto;
import com.credithandler.api.controller.calculator.dto.loan.LoanStatementRequestDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculatingOffers(LoanStatementRequestDto request);
    CreditDto calculateCredit(ScoringDataDto request);
}
