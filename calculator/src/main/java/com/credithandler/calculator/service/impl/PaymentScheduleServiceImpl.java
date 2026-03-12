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
        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal remainingDebt = amount;
        LocalDate date = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal debtPayment;

            if (i == term) {
                debtPayment = remainingDebt;
            } else {
                debtPayment = monthlyPayment.subtract(interestPayment);
            }

            remainingDebt = remainingDebt.subtract(debtPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            PaymentScheduleElementDto element = new PaymentScheduleElementDto(
                    i,
                    date,
                    monthlyPayment,
                    interestPayment,
                    debtPayment.setScale(2, RoundingMode.HALF_UP),
                    remainingDebt
            );

            schedule.add(element);

            date = date.plusMonths(1);
        }
        //TODO: Нужна ли проверка на remainingDebt == 0
        return schedule;
    }
}
