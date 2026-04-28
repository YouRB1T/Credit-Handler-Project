package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;

import java.util.UUID;

public interface DealService {
    StatementDto selectOfferForDeal(LoanOfferDto request);
    StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId);
}
