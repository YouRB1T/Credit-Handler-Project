package com.credithandler.calculator.service;

import com.credithandler.calculator.dto.calc.PaymentScheduleElementDto;
import com.credithandler.calculator.service.impl.PaymentScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование PaymentScheduleServiceImpl")
class PaymentScheduleServiceImplTest {

    private PaymentScheduleServiceImpl paymentScheduleService;

    @BeforeEach
    void setUp() {
        paymentScheduleService = new PaymentScheduleServiceImpl();
    }

    @Test
    @DisplayName("корректный график платежей для стандартного кредита")
    void createLoanPaymentSchedule_withStandardLoan_shouldReturnValidSchedule() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal totalAmount = new BigDecimal("108310.20");
        BigDecimal monthlyPayment = new BigDecimal("9025.85");
        BigDecimal monthlyRate = new BigDecimal("0.0125");
        Integer term = 12;

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        assertThat(schedule).isNotNull();
        assertThat(schedule).hasSize(term);

        PaymentScheduleElementDto firstPayment = schedule.getFirst();
        assertThat(firstPayment.getNumber()).isEqualTo(1);
        assertThat(firstPayment.getDate()).isAfter(LocalDate.now());
        assertThat(firstPayment.getTotalPayment()).isEqualByComparingTo(monthlyPayment);
        assertThat(firstPayment.getInterestPayment()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(firstPayment.getDebtPayment()).isEqualByComparingTo(new BigDecimal("7775.85"));
        assertThat(firstPayment.getRemainingDebt()).isEqualByComparingTo(new BigDecimal("92224.15"));

        PaymentScheduleElementDto lastPayment = schedule.get(term - 1);
        assertThat(lastPayment.getNumber()).isEqualTo(term);
        assertThat(lastPayment.getRemainingDebt()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("График платежей для кредита без процентов")
    void createLoanPaymentSchedule_withZeroRate_shouldReturnEqualPayments() {

        BigDecimal amount = new BigDecimal("120000");
        BigDecimal totalAmount = new BigDecimal("120000");
        BigDecimal monthlyPayment = new BigDecimal("10000");
        BigDecimal monthlyRate = BigDecimal.ZERO;
        int term = 12;

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        assertThat(schedule).hasSize(term);

        for (int i = 0; i < term; i++) {
            PaymentScheduleElementDto payment = schedule.get(i);
            assertThat(payment.getInterestPayment()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(payment.getDebtPayment()).isEqualByComparingTo(monthlyPayment);

            if (i < term - 1) {
                BigDecimal expectedRemaining = amount.subtract(monthlyPayment.multiply(new BigDecimal(i + 1)));
                assertThat(payment.getRemainingDebt()).isEqualByComparingTo(expectedRemaining);
            } else {
                assertThat(payment.getRemainingDebt()).isEqualByComparingTo(BigDecimal.ZERO);
            }
        }
    }

    @Test
    @DisplayName("Корректировка последнего платежа для точного обнуления долга")
    void createLoanPaymentSchedule_lastPaymentShouldZeroRemainingDebt() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal totalAmount = new BigDecimal("108310.20");
        BigDecimal monthlyPayment = new BigDecimal("9025.85");
        BigDecimal monthlyRate = new BigDecimal("0.0125");
        int term = 12;

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        PaymentScheduleElementDto lastPayment = schedule.get(term - 1);
        assertThat(lastPayment.getRemainingDebt()).isEqualByComparingTo(BigDecimal.ZERO);

        BigDecimal sumOfPayments = schedule.stream()
                .map(PaymentScheduleElementDto::getTotalPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sumOfPayments).isEqualByComparingTo(totalAmount);
    }

    @Test
    @DisplayName("Должен создать корректные даты платежей")
    void createLoanPaymentSchedule_shouldGenerateCorrectDates() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal totalAmount = new BigDecimal("108310.20");
        BigDecimal monthlyPayment = new BigDecimal("9025.85");
        BigDecimal monthlyRate = new BigDecimal("0.0125");
        int term = 12;

        LocalDate testStartDate = LocalDate.now();

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        LocalDate expectedDate = testStartDate.plusMonths(1);
        for (int i = 0; i < term; i++) {
            PaymentScheduleElementDto payment = schedule.get(i);
            assertThat(payment.getDate()).isEqualTo(expectedDate);
            expectedDate = expectedDate.plusMonths(1);
        }
    }

    @ParameterizedTest
    @MethodSource("provideLoanScenarios")
    @DisplayName("Тест создания графиков платежей")
    void createLoanPaymentSchedule_withDifferentScenarios_shouldReturnValidSchedule(
            BigDecimal amount, Integer term, BigDecimal monthlyPayment,
            BigDecimal monthlyRate, int expectedSize) {

        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        assertThat(schedule).hasSize(expectedSize);

        assertThat(schedule.get(term - 1).getRemainingDebt())
                .isEqualByComparingTo(BigDecimal.ZERO);

        for (int i = 1; i < term; i++) {
            assertThat(schedule.get(i).getInterestPayment())
                    .isLessThanOrEqualTo(schedule.get(i - 1).getInterestPayment());
        }

        for (int i = 1; i < term; i++) {
            assertThat(schedule.get(i).getDebtPayment())
                    .isGreaterThanOrEqualTo(schedule.get(i - 1).getDebtPayment());
        }
    }

    private static Stream<Arguments> provideLoanScenarios() {
        return Stream.of(
                Arguments.of(new BigDecimal("100000"), 12, new BigDecimal("9025.85"), new BigDecimal("0.0125"), 12),
                Arguments.of(new BigDecimal("50000"), 24, new BigDecimal("2353.67"), new BigDecimal("0.01"), 24),
                Arguments.of(new BigDecimal("200000"), 36, new BigDecimal("7230.48"), new BigDecimal("0.015"), 36),
                Arguments.of(new BigDecimal("30000"), 6, new BigDecimal("5153.44"), new BigDecimal("0.00833"), 6)
        );
    }

    @Test
    @DisplayName("Расчет структуры для каждого платежа")
    void createLoanPaymentSchedule_shouldCalculatePaymentStructureCorrectly() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal monthlyPayment = new BigDecimal("9025.85");
        BigDecimal monthlyRate = new BigDecimal("0.0125");
        int term = 3;

        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        PaymentScheduleElementDto p1 = schedule.getFirst();
        assertThat(p1.getInterestPayment()).isEqualByComparingTo(new BigDecimal("1250.00"));
        assertThat(p1.getDebtPayment()).isEqualByComparingTo(new BigDecimal("7775.85"));
        assertThat(p1.getRemainingDebt()).isEqualByComparingTo(new BigDecimal("92224.15"));
        assertThat(p1.getTotalPayment()).isEqualByComparingTo(monthlyPayment);

        PaymentScheduleElementDto p2 = schedule.get(1);
        BigDecimal expectedInterest2 = new BigDecimal("92224.15").multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedDebt2 = monthlyPayment.subtract(expectedInterest2);
        BigDecimal expectedRemaining2 = p1.getRemainingDebt().subtract(expectedDebt2);

        assertThat(p2.getInterestPayment()).isEqualByComparingTo(expectedInterest2);
        assertThat(p2.getDebtPayment()).isEqualByComparingTo(expectedDebt2);
        assertThat(p2.getRemainingDebt()).isEqualByComparingTo(expectedRemaining2);
        assertThat(p2.getTotalPayment()).isEqualByComparingTo(monthlyPayment);

        PaymentScheduleElementDto p3 = schedule.get(2);
        BigDecimal expectedInterest3 = p2.getRemainingDebt().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedDebt3 = p2.getRemainingDebt();

        assertThat(p3.getInterestPayment()).isEqualByComparingTo(expectedInterest3);
        assertThat(p3.getDebtPayment()).isEqualByComparingTo(expectedDebt3);
        assertThat(p3.getRemainingDebt()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Должен обрабатывать кредит на 1 месяц")
    void createLoanPaymentSchedule_withOneMonthTerm_shouldReturnSinglePayment() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal monthlyPayment = new BigDecimal("115000.00");
        BigDecimal monthlyRate = new BigDecimal("0.15");
        int term = 1;

        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));

        List<PaymentScheduleElementDto> schedule = paymentScheduleService.createLoanPaymentSchedule(
                amount, totalAmount, monthlyPayment, monthlyRate, term);

        assertThat(schedule).hasSize(1);

        PaymentScheduleElementDto payment = schedule.getFirst();
        assertThat(payment.getInterestPayment()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(payment.getDebtPayment()).isEqualByComparingTo(amount);
        assertThat(payment.getRemainingDebt()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(payment.getTotalPayment()).isEqualByComparingTo(monthlyPayment);
    }
}