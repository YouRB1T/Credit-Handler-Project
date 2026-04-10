package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;

import java.util.List;

public interface StatementService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);
}
