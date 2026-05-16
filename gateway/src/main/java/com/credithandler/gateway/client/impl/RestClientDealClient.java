package com.credithandler.gateway.client.impl;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.client.DealClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class RestClientDealClient implements DealClient {

    private final RestClient restClient;

    public RestClientDealClient(@Qualifier("dealRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
        return restClient.post()
                .uri("/deal/calculate/{statementId}", statementId)
                .body(request)
                .retrieve()
                .body(StatementDto.class);
    }

    @Override
    public StatementDto sendDocuments(UUID statementId) {
        return restClient.post()
                .uri("/deal/document/{statementId}/send", statementId)
                .retrieve()
                .body(StatementDto.class);
    }

    @Override
    public StatementDto signDocuments(UUID statementId) {
        return restClient.post()
                .uri("/deal/document/{statementId}/sign", statementId)
                .retrieve()
                .body(StatementDto.class);
    }

    @Override
    public StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request) {
        return restClient.post()
                .uri("/deal/document/{statementId}/code", statementId)
                .body(request)
                .retrieve()
                .body(StatementDto.class);
    }
}
