package com.credithandler.calculator.service;

import com.credithandler.calculator.api.dto.calc.PaymentScheduleElementDto;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentScheduleService {
    List<PaymentScheduleElementDto> createLoanPaymentSchedule(BigDecimal amount, BigDecimal totalPayment,
                                                              BigDecimal monthlyPayment,
                                                              BigDecimal rate, Integer term);
}
