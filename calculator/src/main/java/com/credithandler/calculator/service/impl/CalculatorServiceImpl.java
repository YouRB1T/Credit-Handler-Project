package com.credithandler.calculator.service.impl;

import com.credithandler.api.controller.calculator.dto.calc.CreditDto;
import com.credithandler.api.controller.calculator.dto.calc.ScoringDataDto;
import com.credithandler.api.controller.calculator.dto.loan.LoanOfferDto;
import com.credithandler.api.controller.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorServiceImpl implements CalculatorService {

    private static final boolean[] INSURANCE_OPTIONS = {false, true};
    private static final boolean[] SALARY_OPTIONS = {false, true};

    private final CalculateLoanService loanService;
    private final CalculateCreditService calculateCreditService;

    @Override
    public List<LoanOfferDto> calculatingOffers(LoanStatementRequestDto request) {
        log.info(">> calculatingOffers, request: {}", request);

        List<LoanOfferDto> offers = new ArrayList<>();

        log.debug("Генерация 4 предложений");

        for (boolean insuranceOption : INSURANCE_OPTIONS) {
            for (boolean salaryOption : SALARY_OPTIONS) {
                LoanOfferDto offer = loanService.calculateLoan(
                        request.getAmount(),
                        request.getTerm(),
                        insuranceOption,
                        salaryOption
                );

                offers.add(offer);
                log.debug("Сгенерировано предложение: страхование={}, зарплатный={}, ставка={}%",
                        insuranceOption, salaryOption, offer.getRate());
            }
        }

        offers.sort(Comparator.comparing(LoanOfferDto::getTotalAmount));

        log.info("<< calculatingOffers, offers count: {}, best rate: {}%",
                offers.size(), offers.getFirst().getRate());
        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {
        log.info(">> calculateCredit, request: {}", request);

        CreditDto credit = calculateCreditService.calculateCredit(request);

        log.info("<< calculateCredit, credit: {}", credit);
        return credit;
    }
}