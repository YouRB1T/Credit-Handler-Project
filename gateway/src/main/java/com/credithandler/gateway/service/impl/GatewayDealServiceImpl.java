package com.credithandler.gateway.service.impl;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.client.DealClient;
import com.credithandler.gateway.service.GatewayDealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayDealServiceImpl implements GatewayDealService {

    private final DealClient dealClient;

    @Override
    public StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
        log.info(">> registrationDealAndCountCredit, statementId: {}, request: {}", statementId, request);

        StatementDto statement = dealClient.registrationDealAndCountCredit(request, statementId);

        log.info("<< registrationDealAndCountCredit, statementId: {}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto sendDocuments(UUID statementId) {
        log.info(">> sendDocuments, statementId: {}", statementId);

        StatementDto statement = dealClient.sendDocuments(statementId);

        log.info("<< sendDocuments, statementId: {}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto signDocuments(UUID statementId) {
        log.info(">> signDocuments, statementId: {}", statementId);

        StatementDto statement = dealClient.signDocuments(statementId);

        log.info("<< signDocuments, statementId: {}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request) {
        log.info(">> codeDocuments, statementId: {}", statementId);

        StatementDto statement = dealClient.codeDocuments(statementId, request);

        log.info("<< codeDocuments, statementId: {}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public StatementDto getStatementById(UUID statementId) {
        log.info(">> getStatementById, statementId: {}", statementId);

        StatementDto statement = dealClient.getStatementById(statementId);

        log.info("<< getStatementById, statementId: {}, status: {}", statementId, statement.getStatus());
        return statement;
    }

    @Override
    public List<StatementDto> getAllStatements() {
        log.info(">> getAllStatements");

        List<StatementDto> statements = dealClient.getAllStatements();

        log.info("<< getAllStatements, count: {}", statements.size());
        return statements;
    }
}
