package com.credithandler.gateway.service;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;

import java.util.List;
import java.util.UUID;

public interface GatewayDealService {

    StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId);

    StatementDto sendDocuments(UUID statementId);

    StatementDto signDocuments(UUID statementId);

    StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request);

    StatementDto getStatementById(UUID statementId);

    List<StatementDto> getAllStatements();
}
