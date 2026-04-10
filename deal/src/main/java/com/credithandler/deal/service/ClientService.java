package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.model.Client;

public interface ClientService {
    Client createClient(LoanStatementRequestDto request);
}
