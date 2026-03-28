package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;

import java.util.UUID;

public interface DealService {
    void selectOfferForDeal(LoanOfferDto request);
    void registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId);
}
