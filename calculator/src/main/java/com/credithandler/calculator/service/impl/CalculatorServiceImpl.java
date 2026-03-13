package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.dto.loan.LoanStatementRequestDto;
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

    private final CalculateLoanService loanService;
    private final CalculateCreditService calculateCreditService;

    @Override
    public List<LoanOfferDto> calculatingOffers(LoanStatementRequestDto request) {
        log.info("Получен запрос на расчет предложений: сумма {} руб., срок {} мес.",
                request.getAmount(), request.getTerm());

        List<LoanOfferDto> offers = new ArrayList<>();

        boolean[] insuranceOptions = {false, true};
        boolean[] salaryOptions = {false, true};

        log.debug("Генерация 4 предложений");

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

        log.info("Сгенерировано {} предложений. Лучшая ставка: {}%",
                offers.size(), offers.getFirst().getRate());

        return offers;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {
        log.info("Получен запрос на расчет кредита: сумма {} руб., срок {} мес.",
                request.getAmount(), request.getTerm());

        CreditDto credit = calculateCreditService.calculateCredit(request);

        log.info("Кредит рассчитан: ежемесячный платеж {} руб., ставка {}%",
                credit.getMonthlyPayment(), credit.getRate());

        return credit;
    }
}