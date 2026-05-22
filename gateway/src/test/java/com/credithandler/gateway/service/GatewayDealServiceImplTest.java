package com.credithandler.gateway.service;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.client.DealClient;
import com.credithandler.gateway.service.impl.GatewayDealServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@DisplayName("Тестирование сервиса gateway для deal")

class GatewayDealServiceImplTest {

    private final DealClient dealClient = mock(DealClient.class);
    private final GatewayDealService service = new GatewayDealServiceImpl(dealClient);

    @Test
    @DisplayName("registration делегирует запрос в deal client")
    void registrationDelegatesRequestToDealClient() {
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        StatementDto statement = new StatementDto();
        when(dealClient.registrationDealAndCountCredit(request, statementId)).thenReturn(statement);

        StatementDto result = service.registrationDealAndCountCredit(request, statementId);

        assertThat(result).isSameAs(statement);
        verify(dealClient).registrationDealAndCountCredit(request, statementId);
    }

    @Test
    @DisplayName("sendDocuments делегирует запрос в deal client")
    void sendDocumentsDelegatesRequestToDealClient() {
        UUID statementId = UUID.randomUUID();
        StatementDto statement = new StatementDto();
        when(dealClient.sendDocuments(statementId)).thenReturn(statement);

        StatementDto result = service.sendDocuments(statementId);

        assertThat(result).isSameAs(statement);
        verify(dealClient).sendDocuments(statementId);
    }

    @Test
    @DisplayName("signDocuments делегирует запрос в deal client")
    void signDocumentsDelegatesRequestToDealClient() {
        UUID statementId = UUID.randomUUID();
        StatementDto statement = new StatementDto();
        when(dealClient.signDocuments(statementId)).thenReturn(statement);

        StatementDto result = service.signDocuments(statementId);

        assertThat(result).isSameAs(statement);
        verify(dealClient).signDocuments(statementId);
    }

    @Test
    @DisplayName("codeDocuments делегирует запрос в deal client")
    void codeDocumentsDelegatesRequestToDealClient() {
        UUID statementId = UUID.randomUUID();
        SesCodeRequestDto request = new SesCodeRequestDto();
        StatementDto statement = new StatementDto();
        when(dealClient.codeDocuments(statementId, request)).thenReturn(statement);

        StatementDto result = service.codeDocuments(statementId, request);

        assertThat(result).isSameAs(statement);
        verify(dealClient).codeDocuments(statementId, request);
    }

    @Test
    @DisplayName("getStatementById делегирует запрос в deal client")
    void getStatementByIdDelegatesRequestToDealClient() {
        UUID statementId = UUID.randomUUID();
        StatementDto statement = new StatementDto();
        when(dealClient.getStatementById(statementId)).thenReturn(statement);

        StatementDto result = service.getStatementById(statementId);

        assertThat(result).isSameAs(statement);
        verify(dealClient).getStatementById(statementId);
    }

    @Test
    @DisplayName("getAllStatements делегирует запрос в deal client")
    void getAllStatementsDelegatesRequestToDealClient() {
        List<StatementDto> statements = List.of(new StatementDto());
        when(dealClient.getAllStatements()).thenReturn(statements);

        List<StatementDto> result = service.getAllStatements();

        assertThat(result).isSameAs(statements);
        verify(dealClient).getAllStatements();
    }
}
