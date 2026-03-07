package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.service.CalculateLoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class CalculateLoanServiceImpl implements CalculateLoanService {

    //TODO: пересмотреть переменные окружения и ех взаимодействие со spring
    @Value("${LOAN_INTEREST}")
    private BigDecimal LOAN_INTEREST;
    @Value("${IF_INSURANCE}")
    private BigDecimal IF_INSURANCE;
    @Value("${IF_SALARY}")
    private BigDecimal IF_SALARY;
    @Value("$MAX_TERM")
    private Integer MAX_TERM;

    //TODO: StatementId пока null, исправить позже
    /*
    Логика кредитного предложения. Переменные: сумма кредита, срок кредита, есть ли страхование у клиента, есть ли заработок у клиента
    От срока не зависит
    Если у клиента есть страхование то итоговая сумма кредита меньше на 2%
    Если у клиента есть стабильный заработок то процентная ставка кредита меньше на 1%
     */
    @Override
    public LoanOfferDto calculateLoan(BigDecimal amount, Integer term, Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        validateInputData(amount, term);

        // Изначально идет как ежемесячная
        BigDecimal finalRate = calculateFinalRate(isInsuranceEnabled, isSalaryClient);

        BigDecimal monthlyPayment = calculateMonthlyPayment(amount, finalRate, term);

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

    private BigDecimal calculateFinalRate(Boolean isInsurance, Boolean isSalary) {
        BigDecimal rate = LOAN_INTEREST;

        if (isInsurance) {
            rate = rate.subtract(IF_INSURANCE);
            log.debug("Insurance discount applied: -{}%", IF_INSURANCE);
        }

        if (isSalary) {
            rate = rate.subtract(IF_SALARY);
            log.debug("Salary client discount applied: -{}%", IF_SALARY);
        }

        //TODO: Проверить настолько ли нужна проверка на отрицательность
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            rate = BigDecimal.ZERO;
            log.warn("Interest rate became negative, set to 0%");
        }

        return rate;
    }

    /**
     * Расчет аннуитетного платежа
     *
     * Формула
     * https://www.gazprombank.ru/pro-finance/credit/kak-rasschitat-annuitetnyj-platezh/
     * П = С * (ПС * (1 + ПС) ^ n) / ((1 + ПС) ^ n - 1)
     * П - ежемесячный платеж
     * С - сумма кредита (amount)
     * ПС - месячная процентная ставка (annualRate) PS изначально считаем месячную процентную ставку в методе calculateFinalRate
     * n - кол-во месяцев (term)
     * @param amount
     * @param annualRate
     * @param term
     * @return
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal annualRate, Integer term) {
        BigDecimal monthlyRate = annualRate
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        // (1 + ПС)
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        // (1 + ПС) ^ n
        BigDecimal powTermOnePlusRate = onePlusRate.pow(term);

        // (1 + ПС) ^ n - 1
        BigDecimal powTermOnePlusRateMinusOne = powTermOnePlusRate.subtract(BigDecimal.ONE);

        // ПС * (1 + ПС) ^ n
        BigDecimal powTermOnePlusRateMultiplyRate = monthlyRate.multiply(powTermOnePlusRate);

        // (ПС * (1 + ПС) ^ n) / ((1 + ПС) ^ n - 1)
        BigDecimal annuityFactor = powTermOnePlusRateMultiplyRate.divide(powTermOnePlusRateMinusOne, 10, RoundingMode.HALF_UP);

        // С * (ПС * (1 + ПС) ^ n) / ((1 + ПС) ^ n - 1)
        return amount.multiply(annuityFactor)
                .setScale(2, RoundingMode.HALF_UP);
    }


    private void validateInputData(BigDecimal amount, Integer term) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (term == null || term <= 0 || term > MAX_TERM) {
            throw new IllegalArgumentException("Term must be between 1 and " + MAX_TERM.toString() +"  months");
        }
    }
}
