package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.exception.BusinessException;
import com.credithandler.calculator.service.CalculateMonthlyPaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
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
     * ПС - месячная процентная ставка (annualRate) PS Передаем в метод годовую процентную ставку
     * n - кол-во месяцев (term)
     */
    @Override
    public BigDecimal monthlyPayment(BigDecimal amount, BigDecimal annualRate, Integer term) {

        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Попытка расчета с нулевой процентной ставкой");
            throw BusinessException.of(
                    "Процентная ставка",
                    "Процентная ставка должна быть больше нуля"
            );
        }

        if (term == null || term <= 0) {
            log.error("Попытка расчета с некорректным сроком кредита: {}", term);
            throw BusinessException.of(
                    "Срок кредита",
                    "Срок кредита должен быть положительным числом"
            );
        }

        log.debug("Расчет ежемесячного платежа: сумма={}, ставка={}%, срок={} мес.",
                amount, annualRate, term);

        BigDecimal monthlyRate = annualRate
                .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
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
        BigDecimal result = amount.multiply(annuityFactor)
                .setScale(2, RoundingMode.HALF_UP);

        log.debug("Результат расчета: {}", result);
        return result;
    }
}