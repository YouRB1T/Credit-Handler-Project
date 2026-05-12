package com.credithandler.statement.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.statement.client.DealClient;
import com.credithandler.statement.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final DealClient dealClient;

    @Override
    public List<LoanOfferDto> prescoring(LoanStatementRequestDto requestDto) {
        log.info(">> prescoring: {}", requestDto);

        List<LoanOfferDto> offers = dealClient.calculateStatements(requestDto).getBody();

        log.info("<< prescoring: {}", offers != null ? offers.size() : 0);
        return offers;
    }

    @Override
    public StatementDto selectOffer(LoanOfferDto offerDto) {
        log.info(">> selectOffer: {}", offerDto);

        StatementDto selectedStatement = dealClient.selectOfferForDeal(offerDto).getBody();

        log.info("<< selectOffer: {}",
                selectedStatement != null ? selectedStatement.getStatementId() : "null");
        return selectedStatement;
    }
}