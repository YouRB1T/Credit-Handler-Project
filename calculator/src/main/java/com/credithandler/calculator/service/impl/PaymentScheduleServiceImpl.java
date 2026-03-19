package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.api.dto.calc.PaymentScheduleElementDto;
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

    private static final int RESULT_SCALE = 2;
    private static final int ONE_MONTH = 1;
    private static final int FIRST_PAYMENT_NUMBER = 1;

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
        log.info(">> createLoanPaymentSchedule, amount: {}, term: {}, monthlyPayment: {}",
                amount, term, monthlyPayment);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal remainingDebt = amount;
        LocalDate date = LocalDate.now().plusMonths(ONE_MONTH);

        log.debug("Начальный остаток долга: {} руб., дата первого платежа: {}", remainingDebt, date);

        for (int i = FIRST_PAYMENT_NUMBER; i <= term; i++) {
            log.debug("Расчет платежа №{}", i);

            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate);
            log.debug("Проценты за месяц: {} руб.", interestPayment);

            BigDecimal debtPayment;

            if (i == term) {
                debtPayment = remainingDebt;
                log.debug("Последний месяц: погашение остатка долга {} руб.", debtPayment);
            } else {
                debtPayment = monthlyPayment.subtract(interestPayment);
                log.debug("Погашение основного долга: {} руб.", debtPayment);
            }

            remainingDebt = remainingDebt.subtract(debtPayment);
            log.debug("Остаток долга после платежа: {} руб.", remainingDebt);

            PaymentScheduleElementDto element = new PaymentScheduleElementDto(
                    i,
                    date,
                    monthlyPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                    interestPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                    debtPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP),
                    remainingDebt.setScale(RESULT_SCALE, RoundingMode.HALF_UP)
            );

            schedule.add(element);
            log.trace("Добавлен элемент графика: {}", element);

            date = date.plusMonths(ONE_MONTH);
            log.debug("Следующая дата платежа: {}", date);
        }

        log.info("<< createLoanPaymentSchedule, schedule size: {}, final remaining debt: {} руб.",
                schedule.size(), remainingDebt);

        return schedule;
    }
}