package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.CalculateLoanService;
import com.credithandler.calculator.service.CalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private final CalculateLoanService loanService;

    @Override
    public List<LoanOfferDto> provisionLoanOffers(LoanStatementRequestDto request) {

        return List.of();
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {
        return null;
    }
}
