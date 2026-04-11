package com.credithandler.statement.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;

import java.util.List;

public interface LoanService {
    List<LoanOfferDto> prescoring(LoanStatementRequestDto requestDto);
    StatementDto selectOffer(LoanOfferDto offerDto);
}
