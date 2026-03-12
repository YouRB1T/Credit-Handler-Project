package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.service.CalculateMonthlyPaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculateMonthlyPaymentImpl implements CalculateMonthlyPaymentService {
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
     */
    @Override
    public BigDecimal monthlyPayment(BigDecimal amount, BigDecimal annualRate, Integer term) {
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
}
