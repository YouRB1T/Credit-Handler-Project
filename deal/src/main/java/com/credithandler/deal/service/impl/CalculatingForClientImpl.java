package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.client.CalculatorClient;
import com.credithandler.deal.service.CalculatingForClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatingForClientImpl implements CalculatingForClient {
    private final CalculatorClient calculatorClient;

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto request) {
        log.info(">> getLoanOffers, request: {}", request);

        List<LoanOfferDto> offers = calculatorClient.getLoanOffers(request);

        log.info("<< getLoanOffers, offers count: {}", offers);
        return offers;
    }
}
