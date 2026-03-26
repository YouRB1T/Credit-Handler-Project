package com.credithandler.deal.controller;

import com.credithandler.api.controller.deal.DealController;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DealControllerImpl implements DealController {
    private final StatementService statementService;
    @Override
    public ResponseEntity<List<LoanOfferDto>> calculateStatements(LoanStatementRequestDto request) {
        return ResponseEntity.ok(
                statementService.createStatement(request)
        );
    }

    @Override
    public ResponseEntity<Void> selectOfferForDeal(LoanOfferDto request) {
        return null;
    }

    @Override
    public ResponseEntity<Void> registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
        return null;
    }
}
