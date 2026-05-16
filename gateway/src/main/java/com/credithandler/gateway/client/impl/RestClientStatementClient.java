package com.credithandler.gateway.client.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.client.StatementClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
public class RestClientStatementClient implements StatementClient {

    private final RestClient restClient;

    public RestClientStatementClient(@Qualifier("statementRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info(">> statement POST /statement, request: {}", request);

        List<LoanOfferDto> offers = restClient.post()
                .uri("/statement")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        log.info("<< statement POST /statement, offers count: {}", offers.size());
        return offers;
    }

    @Override
    public StatementDto selectOffer(LoanOfferDto request) {
        log.info(">> statement POST /statement/offer, request: {}", request);

        StatementDto statement = restClient.post()
                .uri("/statement/offer")
                .body(request)
                .retrieve()
                .body(StatementDto.class);

        log.info("<< statement POST /statement/offer, statementId: {}, status: {}",
                statement.getStatementId(), statement.getStatus());
        return statement;
    }
}
