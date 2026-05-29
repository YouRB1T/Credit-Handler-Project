package com.credithandler.calculator.service;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.PaymentScheduleElementDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.calculator.service.impl.CalculateCreditServiceImpl;
import com.credithandler.calculator.service.impl.PaymentScheduleServiceImpl;
import com.credithandler.calculator.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование CalculateCreditServiceImpl")
class CalculateCreditServiceImplTest {

    @Mock
    private CalculateRateService calculateRateService;

    @Mock
    private CalculateMonthlyPaymentService calculateMonthlyPaymentService;

    @Mock
    private PaymentScheduleServiceImpl paymentScheduleService;

    @InjectMocks
    private CalculateCreditServiceImpl calculateCreditService;

    private ScoringDataDto validScoringData;
    private List<PaymentScheduleElementDto> mockSchedule;

    @BeforeEach
    void setUp() {
        validScoringData = TestDataFactory.createValidScoringData();

        PaymentScheduleElementDto element = new PaymentScheduleElementDto(
                1, java.time.LocalDate.now().plusMonths(1),
                new BigDecimal("15000"), new BigDecimal("5000"),
                new BigDecimal("10000"), new BigDecimal("90000")
        );
        mockSchedule = List.of(element);
    }

    @Test
    @DisplayName("Расчет кредита с валидными данными")
    void calculateCredit_withValidData_shouldReturnCreditDto() {

        BigDecimal expectedRate = new BigDecimal("12.5");
        BigDecimal expectedMonthlyPayment = new BigDecimal("15000.00");
        BigDecimal expectedPsk = expectedMonthlyPayment.multiply(new BigDecimal(validScoringData.getTerm()));

        when(calculateRateService.calculateScoringRate(validScoringData)).thenReturn(expectedRate);
        when(calculateMonthlyPaymentService.monthlyPayment(
                validScoringData.getAmount(), expectedRate, validScoringData.getTerm()))
                .thenReturn(expectedMonthlyPayment);
        when(paymentScheduleService.createLoanPaymentSchedule(
                validScoringData.getAmount(), expectedPsk, expectedMonthlyPayment,
                expectedRate, validScoringData.getTerm()))
                .thenReturn(mockSchedule);

        CreditDto result = calculateCreditService.calculateCredit(validScoringData);

        assertThat(result).isNotNull();

        assertThat(result.getAmount())
                .isEqualByComparingTo(validScoringData.getAmount());

        assertThat(result.getTerm())
                .isEqualTo(validScoringData.getTerm());

        assertThat(result.getRate())
                .isEqualByComparingTo(expectedRate);

        assertThat(result.getMonthlyPayment())
                .isEqualByComparingTo(expectedMonthlyPayment);

        assertThat(result.getPsk())
                .isEqualByComparingTo(expectedPsk);

        assertThat(result.getIsInsuranceEnabled())
                .isEqualTo(validScoringData.getIsInsuranceEnabled());
        assertThat(result.getIsSalaryClient())
                .isEqualTo(validScoringData.getIsSalaryClient());

        assertThat(result.getPaymentSchedule())
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        verify(calculateRateService).calculateScoringRate(validScoringData);
        verify(calculateMonthlyPaymentService).monthlyPayment(
                validScoringData.getAmount(), expectedRate, validScoringData.getTerm());
        verify(paymentScheduleService).createLoanPaymentSchedule(
                validScoringData.getAmount(), expectedPsk, expectedMonthlyPayment,
                expectedRate, validScoringData.getTerm());
    }

    @Test
    @DisplayName("Обработка случая без страховки")
    void calculateCredit_withoutInsurance_shouldPassCorrectFlag() {

        ScoringDataDto requestWithoutInsurance = TestDataFactory.createValidScoringData();
        requestWithoutInsurance.setIsInsuranceEnabled(false);

        BigDecimal expectedRate = new BigDecimal("12.5");
        BigDecimal expectedMonthlyPayment = new BigDecimal("15000.00");

        when(calculateRateService.calculateScoringRate(requestWithoutInsurance)).thenReturn(expectedRate);
        when(calculateMonthlyPaymentService.monthlyPayment(
                requestWithoutInsurance.getAmount(), expectedRate, requestWithoutInsurance.getTerm()))
                .thenReturn(expectedMonthlyPayment);
        when(paymentScheduleService.createLoanPaymentSchedule(
                any(), any(), any(), any(), anyInt()))
                .thenReturn(mockSchedule);

        CreditDto result = calculateCreditService.calculateCredit(requestWithoutInsurance);

        assertThat(result.getIsInsuranceEnabled()).isFalse();
        assertThat(result.getIsSalaryClient()).isTrue();
    }

    @Test
    @DisplayName("Обработка случая незарплатного клиента")
    void calculateCredit_withoutSalaryClient_shouldPassCorrectFlag() {

        ScoringDataDto requestWithoutSalary = TestDataFactory.createValidScoringData();
        requestWithoutSalary.setIsSalaryClient(false);

        BigDecimal expectedRate = new BigDecimal("12.5");
        BigDecimal expectedMonthlyPayment = new BigDecimal("15000.00");

        when(calculateRateService.calculateScoringRate(requestWithoutSalary)).thenReturn(expectedRate);
        when(calculateMonthlyPaymentService.monthlyPayment(
                requestWithoutSalary.getAmount(), expectedRate, requestWithoutSalary.getTerm()))
                .thenReturn(expectedMonthlyPayment);
        when(paymentScheduleService.createLoanPaymentSchedule(
                any(), any(), any(), any(), anyInt()))
                .thenReturn(mockSchedule);

        CreditDto result = calculateCreditService.calculateCredit(requestWithoutSalary);

        assertThat(result.getIsInsuranceEnabled()).isTrue();
        assertThat(result.getIsSalaryClient()).isFalse();
    }
}
