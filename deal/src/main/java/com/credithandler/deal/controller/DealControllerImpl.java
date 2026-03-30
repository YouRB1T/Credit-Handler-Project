package com.credithandler.deal.controller;

import com.credithandler.api.controller.deal.DealController;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.service.DealService;
import com.credithandler.deal.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DealControllerImpl implements DealController {
    private final StatementService statementService;
    private final DealService dealService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> calculateStatements(LoanStatementRequestDto request) {
        return ResponseEntity.ok(
                statementService.createStatement(request)
        );
    }

    @Override
    public ResponseEntity<Void> selectOfferForDeal(LoanOfferDto request) {
        dealService.selectOfferForDeal(request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> registrationDealAndCountCredit(
            FinishRegistrationRequestDto request, UUID statementId) {
        dealService.registrationDealAndCountCredit(request, statementId);
        return ResponseEntity.ok().build();
    }
}
