package com.credithandler.gateway.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.client.StatementClient;
import com.credithandler.gateway.service.GatewayStatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GatewayStatementServiceImpl implements GatewayStatementService {

    private final StatementClient statementClient;

    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        return statementClient.createStatement(request);
    }

    @Override
    public StatementDto selectOffer(LoanOfferDto request) {
        return statementClient.selectOffer(request);
    }
}
