package com.credithandler.calculator.service;

import com.credithandler.api.exception.BusinessException;
import com.credithandler.calculator.service.impl.CalculateMonthlyPaymentImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование CalculateMonthlyPaymentImpl")
class CalculateMonthlyPaymentImplTest {

    private CalculateMonthlyPaymentImpl monthlyPaymentService;

    @BeforeEach
    void setUp() {
        monthlyPaymentService = new CalculateMonthlyPaymentImpl();
    }

    @Test
    @DisplayName("Простая проверка расчета платежа на валидных данных")
    void monthlyPayment_withStandardLoan_shouldCalculateCorrectly() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("15.0");
        Integer term = 12;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("9025.83"));
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Проверка BusinessException при нулевой ставке")
    void monthlyPayment_withZeroRate_shouldThrowBusinessException() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = BigDecimal.ZERO;
        Integer term = 12;

        assertThatThrownBy(() -> monthlyPaymentService.monthlyPayment(amount, annualRate, term))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Процентная ставка");
    }

    @Test
    @DisplayName("Проверка расчета для маленькой суммы для маленькой суммы")
    void monthlyPayment_withSmallAmount_shouldCalculateCorrectly() {

        BigDecimal amount = new BigDecimal("5000");
        BigDecimal annualRate = new BigDecimal("15.0");
        Integer term = 6;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("870.17"));
    }

    @Test
    @DisplayName("Проверка расчета для крупной суммы")
    void monthlyPayment_withLargeAmount_shouldCalculateCorrectly() {

        BigDecimal amount = new BigDecimal("5000000");
        BigDecimal annualRate = new BigDecimal("12.5");
        Integer term = 60;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo(new BigDecimal("112489.69"));
    }

    @Test
    @DisplayName("Расчет платежа для срока на 1 месяц")
    void monthlyPayment_withOneMonthTerm_shouldCalculateCorrectly() {
        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("15.0");
        Integer term = 1;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        BigDecimal expected = new BigDecimal("101250.00");

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Проверка расчета при дробной ставке")
    void monthlyPayment_shouldRoundToTwoDecimalPlaces() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("15.5");
        Integer term = 12;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        assertThat(result.scale()).isEqualTo(2);

        BigDecimal multipliedBy100 = result.multiply(new BigDecimal("100"));
        assertThat(multipliedBy100.remainder(BigDecimal.ONE))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Проверка маленькой процентной ставки")
    void monthlyPayment_withVerySmallRate_shouldCalculateCorrectly() {

        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("0.1");
        Integer term = 12;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        // Assert
        BigDecimal expected = new BigDecimal("8337.85");
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Проверка для большого срока")
    void monthlyPayment_withMaxTerm_shouldCalculateCorrectly() {
        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("15.0");
        Integer term = 360;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        assertThat(result).isNotNull();
        assertThat(result).isLessThan(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("Проверка отрицательной суммы кредита")
    void monthlyPayment_withNegativeAmount_shouldReturnNegativePayment() {
        BigDecimal amount = new BigDecimal("-100000");
        BigDecimal annualRate = new BigDecimal("7.5");
        Integer term = 12;

        BigDecimal result = monthlyPaymentService.monthlyPayment(amount, annualRate, term);

        assertThat(result).isNegative();
        assertThat(result.abs()).isEqualByComparingTo(new BigDecimal("8675.74"));
    }

    @Test
    @DisplayName("Проверка математической формулы - сумма платежей должна быть больше тела кредита")
    void monthlyPayment_totalPaymentsShouldBeGreaterThanPrincipal() {
        BigDecimal amount = new BigDecimal("100000");
        BigDecimal annualRate = new BigDecimal("15.0");
        int term = 12;

        BigDecimal monthlyPayment = monthlyPaymentService.monthlyPayment(amount, annualRate, term);
        BigDecimal totalPayments = monthlyPayment.multiply(new BigDecimal(term));

        assertThat(totalPayments).isGreaterThan(amount);
    }
}