package com.credithandler.deal.service;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.model.UpdateStatementStatusRequestDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;

import java.util.UUID;

public interface DealService {
    StatementDto selectOfferForDeal(LoanOfferDto request);
    StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId);
    StatementDto sendDocuments(UUID statementId);
    StatementDto signDocuments(UUID statementId);
    StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request);
    StatementDto updateStatementStatus(UUID statementId, UpdateStatementStatusRequestDto request);
}
