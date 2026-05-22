package com.credithandler.gateway.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.client.StatementClient;
import com.credithandler.gateway.service.impl.GatewayStatementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@DisplayName("Тестирование сервиса gateway для statement")

class GatewayStatementServiceImplTest {

    private final StatementClient statementClient = mock(StatementClient.class);
    private final GatewayStatementService service = new GatewayStatementServiceImpl(statementClient);

    @Test
    @DisplayName("createStatement делегирует запрос в statement client")
    void createStatementDelegatesRequestToStatementClient() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        List<LoanOfferDto> offers = List.of(new LoanOfferDto());
        when(statementClient.createStatement(request)).thenReturn(offers);

        List<LoanOfferDto> result = service.createStatement(request);

        assertThat(result).isSameAs(offers);
        verify(statementClient).createStatement(request);
    }

    @Test
    @DisplayName("selectOffer делегирует запрос в statement client")
    void selectOfferDelegatesRequestToStatementClient() {
        LoanOfferDto request = new LoanOfferDto();
        StatementDto statement = new StatementDto();
        when(statementClient.selectOffer(request)).thenReturn(statement);

        StatementDto result = service.selectOffer(request);

        assertThat(result).isSameAs(statement);
        verify(statementClient).selectOffer(request);
    }
}
