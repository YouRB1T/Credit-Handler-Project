package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.PaymentScheduleElementDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.service.CalculateCreditService;
import com.credithandler.calculator.service.CalculateMonthlyPaymentService;
import com.credithandler.calculator.service.CalculateRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalculateCreditServiceImpl implements CalculateCreditService {

    private final CalculateRateService calculateRateService;
    private final CalculateMonthlyPaymentService calculateMonthlyPaymentService;
    private final PaymentScheduleServiceImpl paymentScheduleService;

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {
        log.info("Расчет параметров кредита: сумма {} руб., срок {} мес.",
                request.getAmount(), request.getTerm());

        BigDecimal rate = calculateRateService.calculateScoringRate(request);
        log.debug("Итоговая процентная ставка после скоринга: {}%", rate);

        BigDecimal monthlyPayment = calculateMonthlyPaymentService.monthlyPayment(
                request.getAmount(), rate, request.getTerm()
        );
        log.debug("Ежемесячный платеж: {} руб.", monthlyPayment);

        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(request.getTerm()));
        log.debug("Общая сумма кредита: {} руб.", totalAmount);

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                request.getAmount(), totalAmount, monthlyPayment, rate, request.getTerm()
        );
        log.debug("Сформирован график платежей: {} элементов", schedule.size());

        CreditDto credit = new CreditDto(
                request.getAmount(),
                request.getTerm(),
                monthlyPayment,
                rate,
                totalAmount,
                request.getIsInsuranceEnabled(),
                request.getIsSalaryClient(),
                schedule
        );

        log.info("Кредит рассчитан: ставка {}%, ежемесячный платеж {} руб., общая сумма {} руб.",
                rate, monthlyPayment, totalAmount);

        return credit;
    }
}