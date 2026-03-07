package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.CalculateLoanService;
import com.credithandler.calculator.service.CalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private final CalculateLoanService loanService;

    @Override
    public List<LoanOfferDto> provisionLoanOffers(LoanStatementRequestDto request) {
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

        offers.sort((l1, l2) -> l1.getTotalAmount().compareTo(l2.getTotalAmount()));

        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {

        return null;
    }
}
