package com.credithandler.gateway.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;

import java.util.List;

public interface GatewayStatementService {

    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);

    StatementDto selectOffer(LoanOfferDto request);
}
