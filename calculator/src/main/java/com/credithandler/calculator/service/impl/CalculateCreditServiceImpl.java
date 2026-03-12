package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.PaymentScheduleElementDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.service.CalculateCreditService;
import com.credithandler.calculator.service.CalculateLoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalculateCreditServiceImpl implements CalculateCreditService {

    private final CalculateLoanService calculateLoanService;
    private final PaymentScheduleServiceImpl paymentScheduleService;

    @Override
    public CreditDto calculateCredit(ScoringDataDto request) {

        LoanOfferDto offer = calculateLoanService.calculateLoan(
                request.getAmount(),
                request.getTerm(),
                request.getIsInsuranceEnabled(),
                request.getIsSalaryClient()
        );

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
               request.getAmount(), offer.getTotalAmount(), offer.getMonthlyPayment(), offer.getRate(), request.getTerm()
        );

        return new CreditDto(
                request.getAmount(),
                request.getTerm(),
                offer.getMonthlyPayment(),
                offer.getRate(),
                offer.getTotalAmount(),
                request.getIsInsuranceEnabled(),
                request.getIsSalaryClient(),
                schedule
        );
    }
}
