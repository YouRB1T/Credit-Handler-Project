package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.service.CalculateLoanService;
import com.credithandler.calculator.service.CalculateMonthlyPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateLoanServiceImpl implements CalculateLoanService {

    private final CalculateMonthlyPayment monthlyPaymentCalculator;

    @Value("${loan.base-interest}")
    private BigDecimal LOAN_INTEREST;
    @Value("${loan.insurance-decrease}")
    private BigDecimal IF_INSURANCE;
    @Value("${loan.salary-decrease}")
    private BigDecimal IF_SALARY;

    //TODO: StatementId пока null, исправить позже
    /*
    Логика кредитного предложения. Переменные: сумма кредита, срок кредита, есть ли страхование у клиента, есть ли заработок у клиента
    От срока не зависит
    Если у клиента есть страхование то итоговая сумма кредита меньше на 2%
    Если у клиента есть стабильный заработок то процентная ставка кредита меньше на 1%
     */
    @Override
    public LoanOfferDto calculateLoan(BigDecimal amount, Integer term, Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        // Изначально идет как ежемесячная
        BigDecimal finalRate = calculateRateForLoans(isInsuranceEnabled, isSalaryClient);

        BigDecimal monthlyPayment = monthlyPaymentCalculator.monthlyPayment(amount, finalRate, term);

        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));

        return new LoanOfferDto(
                null,
                amount,
                totalAmount,
                term,
                monthlyPayment,
                finalRate,
                isInsuranceEnabled,
                isSalaryClient

        );
    }

    private BigDecimal calculateRateForLoans(Boolean isInsurance, Boolean isSalary) {
        BigDecimal rate = LOAN_INTEREST;

        if (isInsurance) {
            rate = rate.subtract(IF_INSURANCE);
            log.debug("Insurance discount applied: -{}%", IF_INSURANCE);
        }

        if (isSalary) {
            rate = rate.subtract(IF_SALARY);
            log.debug("Salary client discount applied: -{}%", IF_SALARY);
        }

        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            rate = BigDecimal.ZERO;
            log.warn("Interest rate became negative, set to 0%");
        }

        return rate;
    }
}
