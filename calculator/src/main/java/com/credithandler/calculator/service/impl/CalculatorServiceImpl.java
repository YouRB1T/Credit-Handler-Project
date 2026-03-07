package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.CalculatorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculatorServiceImpl implements CalculatorService {
    @Override
    public List<LoanOfferDto> countCreditLoans(LoanStatementRequestDto request) {
        return List.of();
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {
        return null;
    }
}
