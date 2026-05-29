package com.credithandler.calculator.service.impl;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.calculator.service.CalculateMonthlyPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class CalculateMonthlyPaymentImpl implements CalculateMonthlyPaymentService {

    private static final String INCORRECT_ANNUAL_RATE = "Процентная ставка некорректна";
    private static final String ANNUAL_RATE_MORE_THEN_ZERO = "Процентная ставка должна быть больше 0";

    private static final int CALCULATION_SCALE = 10;
    private static final int RESULT_SCALE = 2;
    private static final BigDecimal PERCENTAGE_DIVIDER = new BigDecimal("100");
    private static final BigDecimal MONTHES = new BigDecimal("12");

    /**
     * Расчет аннуитетного платежа
     * Формула
     * https://www.gazprombank.ru/pro-finance/credit/kak-rasschitat-annuitetnyj-platezh/
     * П = С * (ПС * (1 + ПС) ^ n) / ((1 + ПС) ^ n - 1)
     * П - ежемесячный платеж
     * С - сумма кредита (amount)
     * ПС - месячная процентная ставка (annualRate) PS Передаем в метод годовую процентную ставку
     * n - кол-во месяцев (term)
     */
    @Override
    public BigDecimal monthlyPayment(BigDecimal amount, BigDecimal annualRate, Integer term) {
        log.info(">> monthlyPayment, amount: {}, annualRate: {}, term: {}", amount, annualRate, term);

        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.of(
                    INCORRECT_ANNUAL_RATE,
                    ANNUAL_RATE_MORE_THEN_ZERO
            );
        }

        BigDecimal monthlyRate = annualRate
                .divide(MONTHES, CALCULATION_SCALE, RoundingMode.HALF_UP)
                .divide(PERCENTAGE_DIVIDER, CALCULATION_SCALE, RoundingMode.HALF_UP);

        // (1 + ПС)
        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        // (1 + ПС) ^ n
        BigDecimal powTermOnePlusRate = onePlusRate.pow(term);

        // (1 + ПС) ^ n - 1
        BigDecimal powTermOnePlusRateMinusOne = powTermOnePlusRate.subtract(BigDecimal.ONE);

        // ПС * (1 + ПС) ^ n
        BigDecimal powTermOnePlusRateMultiplyRate = monthlyRate.multiply(powTermOnePlusRate);

        // (ПС * (1 + ПС) ^ n) / ((1 + ПС) ^ n - 1)
        BigDecimal annuityFactor = powTermOnePlusRateMultiplyRate
                .divide(powTermOnePlusRateMinusOne, CALCULATION_SCALE, RoundingMode.HALF_UP);

        // С * (ПС * (1 + ПС) ^ n) / ((1 + ПС) ^ n - 1)
        BigDecimal result = amount.multiply(annuityFactor)
                .setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        log.info("<< monthlyPayment, result: {}", result);

        return result;
    }
}
