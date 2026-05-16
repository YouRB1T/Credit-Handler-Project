package com.credithandler.gateway.client.impl;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.client.DealClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class RestClientDealClient implements DealClient {

    private final RestClient restClient;

    public RestClientDealClient(@Qualifier("dealRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
        log.info(">> deal POST /deal/calculate/{}, request: {}", statementId, request);

        StatementDto statement = restClient.post()
                .uri("/deal/calculate/{statementId}", statementId)
                .body(request)
                .retrieve()
                .body(StatementDto.class);

        log.info("<< deal POST /deal/calculate/{}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto sendDocuments(UUID statementId) {
        log.info(">> deal POST /deal/document/{}/send", statementId);

        StatementDto statement = restClient.post()
                .uri("/deal/document/{statementId}/send", statementId)
                .retrieve()
                .body(StatementDto.class);

        log.info("<< deal POST /deal/document/{}/send, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto signDocuments(UUID statementId) {
        log.info(">> deal POST /deal/document/{}/sign", statementId);

        StatementDto statement = restClient.post()
                .uri("/deal/document/{statementId}/sign", statementId)
                .retrieve()
                .body(StatementDto.class);

        log.info("<< deal POST /deal/document/{}/sign, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request) {
        log.info(">> deal POST /deal/document/{}/code", statementId);

        StatementDto statement = restClient.post()
                .uri("/deal/document/{statementId}/code", statementId)
                .body(request)
                .retrieve()
                .body(StatementDto.class);

        log.info("<< deal POST /deal/document/{}/code, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto getStatementById(UUID statementId) {
        log.info(">> deal GET /deal/admin/statement/{}", statementId);

        StatementDto statement = restClient.get()
                .uri("/deal/admin/statement/{statementId}", statementId)
                .retrieve()
                .body(StatementDto.class);

        log.info("<< deal GET /deal/admin/statement/{}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public List<StatementDto> getAllStatements() {
        log.info(">> deal GET /deal/admin/statement");

        List<StatementDto> statements = restClient.get()
                .uri("/deal/admin/statement")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        log.info("<< deal GET /deal/admin/statement, count: {}", statements.size());
        return statements;
    }
}
