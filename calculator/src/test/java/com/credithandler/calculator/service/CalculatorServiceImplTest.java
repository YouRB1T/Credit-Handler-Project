package com.credithandler.calculator.service;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.impl.CalculatorServiceImpl;
import com.credithandler.calculator.utils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование CalculatorServiceImpl")
class CalculatorServiceImplTest {

    @Mock
    private CalculateLoanService loanService;

    @Mock
    private CalculateCreditService calculateCreditService;

    @InjectMocks
    private CalculatorServiceImpl calculatorService;

    @Test
    @DisplayName("Генерация предложений создает 4 предложения и сортирует по totalAmount")
    void calculatingOffers_shouldGenerateFourOffersAndSortByTotalAmount() {
        LoanStatementRequestDto request = TestDataFactory.createValidLoanStatementRequest();

        LoanOfferDto withoutInsuranceWithoutSalary = offer("140000", "15", false, false);
        LoanOfferDto withoutInsuranceWithSalary = offer("130000", "14", false, true);
        LoanOfferDto withInsuranceWithoutSalary = offer("120000", "13", true, false);
        LoanOfferDto withInsuranceWithSalary = offer("110000", "12", true, true);

        when(loanService.calculateLoan(request.getAmount(), request.getTerm(), false, false))
                .thenReturn(withoutInsuranceWithoutSalary);
        when(loanService.calculateLoan(request.getAmount(), request.getTerm(), false, true))
                .thenReturn(withoutInsuranceWithSalary);
        when(loanService.calculateLoan(request.getAmount(), request.getTerm(), true, false))
                .thenReturn(withInsuranceWithoutSalary);
        when(loanService.calculateLoan(request.getAmount(), request.getTerm(), true, true))
                .thenReturn(withInsuranceWithSalary);

        List<LoanOfferDto> result = calculatorService.calculatingOffers(request);

        assertThat(result)
                .hasSize(4)
                .extracting(LoanOfferDto::getTotalAmount)
                .containsExactly(
                        new BigDecimal("110000"),
                        new BigDecimal("120000"),
                        new BigDecimal("130000"),
                        new BigDecimal("140000")
                );

        assertThat(result.getFirst().getIsInsuranceEnabled()).isTrue();
        assertThat(result.getFirst().getIsSalaryClient()).isTrue();

        verify(loanService).calculateLoan(request.getAmount(), request.getTerm(), false, false);
        verify(loanService).calculateLoan(request.getAmount(), request.getTerm(), false, true);
        verify(loanService).calculateLoan(request.getAmount(), request.getTerm(), true, false);
        verify(loanService).calculateLoan(request.getAmount(), request.getTerm(), true, true);
    }

    @Test
    @DisplayName("Расчет кредита делегируется в CalculateCreditService")
    void calculateCredit_shouldDelegateToCalculateCreditService() {
        ScoringDataDto request = TestDataFactory.createValidScoringData();
        CreditDto expectedCredit = new CreditDto();

        when(calculateCreditService.calculateCredit(request)).thenReturn(expectedCredit);

        CreditDto result = calculatorService.calculateCredit(request);

        assertThat(result).isSameAs(expectedCredit);
        verify(calculateCreditService).calculateCredit(request);
    }

    private LoanOfferDto offer(String totalAmount, String rate, boolean insuranceEnabled, boolean salaryClient) {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setTotalAmount(new BigDecimal(totalAmount));
        offer.setRate(new BigDecimal(rate));
        offer.setIsInsuranceEnabled(insuranceEnabled);
        offer.setIsSalaryClient(salaryClient);
        return offer;
    }
}
