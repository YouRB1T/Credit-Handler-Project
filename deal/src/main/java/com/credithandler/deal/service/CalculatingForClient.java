package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;

import java.util.List;

public interface CalculatingForClient {
    List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto request);
}
