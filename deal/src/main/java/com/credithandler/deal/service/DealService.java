package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;

public interface DealService {
    void selectOfferForDeal(LoanOfferDto request);
}
