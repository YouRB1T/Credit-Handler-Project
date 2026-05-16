package com.credithandler.gateway.client.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.client.StatementClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class RestClientStatementClient implements StatementClient {

    private final RestClient restClient;

    public RestClientStatementClient(@Qualifier("statementRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        return restClient.post()
                .uri("/statement")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }

    @Override
    public StatementDto selectOffer(LoanOfferDto request) {
        return restClient.post()
                .uri("/statement/offer")
                .body(request)
                .retrieve()
                .body(StatementDto.class);
    }
}
