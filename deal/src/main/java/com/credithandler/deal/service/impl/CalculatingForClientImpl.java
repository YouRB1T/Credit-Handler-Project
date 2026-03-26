package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.service.CalculatingForClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatingForClientImpl implements CalculatingForClient {
    private final RestClient restClient;

    @Value("${calculator.url}")
    private String calculatorUrl;

    public List<LoanOfferDto> getLoanOffers(LoanStatementRequestDto request) {
        log.info(">> getLoanOffers, request: {}", request);

            List<LoanOfferDto> offers = restClient.post()
                    .uri(calculatorUrl + "/calculator/offers")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});

        log.info("<< getLoanOffers, offers count: {}", offers != null ? offers.size() : 0);
        return offers;
    }
}
