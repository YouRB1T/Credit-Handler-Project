package com.credithandler.calculator.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.calculator.service.CalculateLoanService;
import com.credithandler.calculator.service.CalculateMonthlyPaymentService;
import com.credithandler.calculator.service.CalculateRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateLoanServiceImpl implements CalculateLoanService {

    private final CalculateMonthlyPaymentService monthlyPaymentCalculator;
    private final CalculateRateService rateCalculator;

    /*
    Логика кредитного предложения. Переменные: сумма кредита, срок кредита, есть ли страхование у клиента, есть ли заработок у клиента
    От срока не зависит
    Если у клиента есть страхование то итоговая сумма кредита меньше на 2%
    Если у клиента есть стабильный заработок то процентная ставка кредита меньше на 1%
     */
    @Override
    public LoanOfferDto calculateLoan(BigDecimal amount, Integer term, Boolean isInsuranceEnabled, Boolean isSalaryClient) {
        log.info(">> calculateLoan, amount: {}, term: {}, isInsuranceEnabled: {}, isSalaryClient: {}",
                amount, term, isInsuranceEnabled, isSalaryClient);

        BigDecimal finalRate = rateCalculator.calculatePrescoringRate(isInsuranceEnabled, isSalaryClient);
        log.debug("Итоговая процентная ставка: {}%", finalRate);

        BigDecimal monthlyPayment = monthlyPaymentCalculator.monthlyPayment(amount, finalRate, term);
        log.debug("Ежемесячный платеж: {} руб.", monthlyPayment);

        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));
        log.debug("Общая сумма кредита: {} руб.", totalAmount);

        LoanOfferDto result = new LoanOfferDto(
                null,
                amount,
                totalAmount,
                term,
                monthlyPayment,
                finalRate,
                isInsuranceEnabled,
                isSalaryClient
        );

        log.info("<< calculateLoan, loanOfferDto: {}", result);
        return result;
    }
}