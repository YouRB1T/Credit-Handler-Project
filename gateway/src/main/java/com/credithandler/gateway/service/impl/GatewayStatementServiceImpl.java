package com.credithandler.gateway.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.client.StatementClient;
import com.credithandler.gateway.service.GatewayStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayStatementServiceImpl implements GatewayStatementService {

    private final StatementClient statementClient;

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info(">> createStatement, request: {}", request);

        List<LoanOfferDto> offers = statementClient.createStatement(request);

        log.info("<< createStatement, offers count: {}", offers.size());
        return offers;
    }

    @Override
    public StatementDto selectOffer(LoanOfferDto request) {
        log.info(">> selectOffer, request: {}", request);

        StatementDto statement = statementClient.selectOffer(request);

        log.info("<< selectOffer, statementId: {}, status: {}", statement.getStatementId(), statement.getStatus());
        return statement;
    }
}
