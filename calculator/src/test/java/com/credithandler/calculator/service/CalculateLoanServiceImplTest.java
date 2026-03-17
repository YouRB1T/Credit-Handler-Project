package com.credithandler.calculator.service;

import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.service.impl.CalculateLoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование CalculateLoanServiceImpl")
class CalculateLoanServiceImplTest {

    @Mock
    private CalculateMonthlyPaymentService monthlyPaymentCalculator;

    @Mock
    private CalculateRateService rateCalculator;

    @InjectMocks
    private CalculateLoanServiceImpl calculateLoanService;

    private BigDecimal amount;
    private Integer term;

    @BeforeEach
    void setUp() {
        amount = new BigDecimal("100000");
        term = 12;
    }

    @Test
    @DisplayName("Расчет кредитного предложения со всеми включенными опциями")
    void calculateLoan_withAllOptions_shouldReturnLoanOffer() {
        Boolean isInsuranceEnabled = true;
        Boolean isSalaryClient = true;

        BigDecimal expectedRate = new BigDecimal("12.0");
        BigDecimal expectedMonthlyPayment = new BigDecimal("8884.88");
        BigDecimal expectedTotalAmount = expectedMonthlyPayment.multiply(new BigDecimal(term));

        when(rateCalculator.calculatePrescoringRate(isInsuranceEnabled, isSalaryClient))
                .thenReturn(expectedRate);
        when(monthlyPaymentCalculator.monthlyPayment(amount, expectedRate, term))
                .thenReturn(expectedMonthlyPayment);

        LoanOfferDto result = calculateLoanService.calculateLoan(
                amount, term, isInsuranceEnabled, isSalaryClient);

        assertThat(result).isNotNull();

        assertThat(result.getRequestedAmount()).isEqualByComparingTo(amount);
        assertThat(result.getTerm()).isEqualTo(term);
        assertThat(result.getRate()).isEqualByComparingTo(expectedRate);
        assertThat(result.getMonthlyPayment()).isEqualByComparingTo(expectedMonthlyPayment);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(expectedTotalAmount);
        assertThat(result.getIsInsuranceEnabled()).isTrue();
        assertThat(result.getIsSalaryClient()).isTrue();

        verify(rateCalculator).calculatePrescoringRate(isInsuranceEnabled, isSalaryClient);
        verify(monthlyPaymentCalculator).monthlyPayment(amount, expectedRate, term);
        verifyNoMoreInteractions(rateCalculator, monthlyPaymentCalculator);
    }

    @Test
    @DisplayName("Расчет кредитного предложения только со страховкой")
    void calculateLoan_withOnlyInsurance_shouldReturnLoanOffer() {

        Boolean isInsuranceEnabled = true;
        Boolean isSalaryClient = false;

        BigDecimal expectedRate = new BigDecimal("13.0");
        BigDecimal expectedMonthlyPayment = new BigDecimal("8937.12");
        BigDecimal expectedTotalAmount = expectedMonthlyPayment.multiply(new BigDecimal(term));

        when(rateCalculator.calculatePrescoringRate(isInsuranceEnabled, isSalaryClient))
                .thenReturn(expectedRate);
        when(monthlyPaymentCalculator.monthlyPayment(amount, expectedRate, term))
                .thenReturn(expectedMonthlyPayment);

        LoanOfferDto result = calculateLoanService.calculateLoan(
                amount, term, isInsuranceEnabled, isSalaryClient);

        assertThat(result).isNotNull();
        assertThat(result.getRate()).isEqualByComparingTo(expectedRate);
        assertThat(result.getMonthlyPayment()).isEqualByComparingTo(expectedMonthlyPayment);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(expectedTotalAmount);
        assertThat(result.getIsInsuranceEnabled()).isTrue();
        assertThat(result.getIsSalaryClient()).isFalse();
    }

    @Test
    @DisplayName("Расчет кредитного предложения только для зарплатного клиента")
    void calculateLoan_withOnlySalaryClient_shouldReturnLoanOffer() {

        Boolean isInsuranceEnabled = false;
        Boolean isSalaryClient = true;

        BigDecimal expectedRate = new BigDecimal("14.0");
        BigDecimal expectedMonthlyPayment = new BigDecimal("8954.12");
        BigDecimal expectedTotalAmount = expectedMonthlyPayment.multiply(new BigDecimal(term));

        when(rateCalculator.calculatePrescoringRate(isInsuranceEnabled, isSalaryClient))
                .thenReturn(expectedRate);
        when(monthlyPaymentCalculator.monthlyPayment(amount, expectedRate, term))
                .thenReturn(expectedMonthlyPayment);

        LoanOfferDto result = calculateLoanService.calculateLoan(
                amount, term, isInsuranceEnabled, isSalaryClient);

        assertThat(result).isNotNull();
        assertThat(result.getRate()).isEqualByComparingTo(expectedRate);
        assertThat(result.getMonthlyPayment()).isEqualByComparingTo(expectedMonthlyPayment);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(expectedTotalAmount);
        assertThat(result.getIsInsuranceEnabled()).isFalse();
        assertThat(result.getIsSalaryClient()).isTrue();
    }

    @Test
    @DisplayName("Расчет без опций")
    void calculateLoan_withoutOptions_shouldReturnLoanOffer() {
        Boolean isInsuranceEnabled = false;
        Boolean isSalaryClient = false;

        BigDecimal expectedRate = new BigDecimal("15.0");
        BigDecimal expectedMonthlyPayment = new BigDecimal("9025.85");
        BigDecimal expectedTotalAmount = expectedMonthlyPayment.multiply(new BigDecimal(term));

        when(rateCalculator.calculatePrescoringRate(isInsuranceEnabled, isSalaryClient))
                .thenReturn(expectedRate);
        when(monthlyPaymentCalculator.monthlyPayment(amount, expectedRate, term))
                .thenReturn(expectedMonthlyPayment);

        LoanOfferDto result = calculateLoanService.calculateLoan(
                amount, term, isInsuranceEnabled, isSalaryClient);

        assertThat(result).isNotNull();
        assertThat(result.getRate()).isEqualByComparingTo(expectedRate);
        assertThat(result.getMonthlyPayment()).isEqualByComparingTo(expectedMonthlyPayment);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(expectedTotalAmount);
        assertThat(result.getIsInsuranceEnabled()).isFalse();
        assertThat(result.getIsSalaryClient()).isFalse();
    }

}