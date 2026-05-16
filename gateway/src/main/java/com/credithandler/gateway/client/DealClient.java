package com.credithandler.gateway.client;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;

import java.util.UUID;

public interface DealClient {

    StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId);

    StatementDto sendDocuments(UUID statementId);

    StatementDto signDocuments(UUID statementId);

    StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request);
}
