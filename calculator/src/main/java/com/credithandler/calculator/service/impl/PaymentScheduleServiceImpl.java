package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.PaymentScheduleElementDto;
import com.credithandler.calculator.service.PaymentScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PaymentScheduleServiceImpl implements PaymentScheduleService {

    /*
    Параметры для подсчета
    общая сумма месячного платежа (total payment)
    сумма по процентам от платежа (interestPayment) (умножаем оставшуюся суму долга на процент)
    сумма погашения основного долга от платежа (debtPayment)
    остаток долга (remainingDebt)
     */
    @Override
    public List<PaymentScheduleElementDto> createLoanPaymentSchedule(BigDecimal amount,
                                                                     BigDecimal totalAmount,
                                                                     BigDecimal monthlyPayment,
                                                                     BigDecimal monthlyRate,
                                                                     Integer term) {
        log.info("Формирование графика платежей: сумма кредита {} руб., срок {} мес., ежемесячный платеж {} руб.",
                amount, term, monthlyPayment);
        log.debug("Общая сумма с процентами: {} руб., месячная ставка: {}%",
                totalAmount, monthlyRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal remainingDebt = amount;
        LocalDate date = LocalDate.now().plusMonths(1);

        log.debug("Начальный остаток долга: {} руб., дата первого платежа: {}", remainingDebt, date);

        for (int i = 1; i <= term; i++) {
            log.debug("Расчет платежа №{}", i);

            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);
            log.debug("Проценты за месяц: {} руб.", interestPayment);

            BigDecimal debtPayment;

            if (i == term) {
                debtPayment = remainingDebt;
                log.debug("Последний месяц: погашение остатка долга {} руб.", debtPayment);
            } else {
                debtPayment = monthlyPayment.subtract(interestPayment);
                log.debug("Погашение основного долга: {} руб.", debtPayment);
            }

            remainingDebt = remainingDebt.subtract(debtPayment)
                    .setScale(2, RoundingMode.HALF_UP);
            log.debug("Остаток долга после платежа: {} руб.", remainingDebt);

            PaymentScheduleElementDto element = new PaymentScheduleElementDto(
                    i,
                    date,
                    monthlyPayment,
                    interestPayment,
                    debtPayment.setScale(2, RoundingMode.HALF_UP),
                    remainingDebt
            );

            schedule.add(element);
            log.trace("Добавлен элемент графика: {}", element);

            date = date.plusMonths(1);
            log.debug("Следующая дата платежа: {}", date);
        }

        log.info("График платежей сформирован: {} элементов. Итоговый остаток: {} руб.",
                schedule.size(), remainingDebt);

        return schedule;
    }
}