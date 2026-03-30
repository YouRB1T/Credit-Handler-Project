package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.client.CalculatorClient;
import com.credithandler.deal.service.impl.CalculatingForClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование CalculatingForClientImpl")
class CalculatingForClientImplTest {

    @Mock
    private CalculatorClient calculatorClient;

    @InjectMocks
    private CalculatingForClientImpl service;

    private LoanStatementRequestDto request;
    private List<LoanOfferDto> offers;

    @BeforeEach
    void setUp() {
        request = new LoanStatementRequestDto();
        offers = List.of(new LoanOfferDto(), new LoanOfferDto());
    }

    @Test
    @DisplayName("Должен вернуть список предложений")
    void getLoanOffers_shouldReturnOffers() {
        when(calculatorClient.getLoanOffers(request)).thenReturn(offers);

        List<LoanOfferDto> result = service.getLoanOffers(request);

        assertThat(result).isNotNull().hasSize(2);
        verify(calculatorClient).getLoanOffers(request);
    }
}
