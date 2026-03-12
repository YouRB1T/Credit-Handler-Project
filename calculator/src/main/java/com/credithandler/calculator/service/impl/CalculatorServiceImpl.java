package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private final CalculateLoanService loanService;
    private final CalculateCreditService calculateCreditService;

    @Override
    public List<LoanOfferDto> calculatingOffers(LoanStatementRequestDto request) {

        List<LoanOfferDto> offers = new ArrayList<>();

        boolean[] insuranceOptions = {false, true};
        boolean[] salaryOptions = {false, true};

        for (boolean insuranceOption : insuranceOptions) {
            for (boolean salaryOption : salaryOptions) {
                LoanOfferDto offer = loanService.calculateLoan(
                        request.getAmount(),
                        request.getTerm(),
                        insuranceOption,
                        salaryOption
                );

                offers.add(offer);
            }
        }

        offers.sort(Comparator.comparing(LoanOfferDto::getTotalAmount));

        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {
        return calculateCreditService.calculateCredit(request);
    }
}
