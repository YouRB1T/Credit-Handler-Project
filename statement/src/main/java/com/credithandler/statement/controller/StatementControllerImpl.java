package com.credithandler.statement.controller;

import com.credithandler.api.controller.StatementController;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.statement.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statement")
@RequiredArgsConstructor
public class StatementControllerImpl implements StatementController {

    private final LoanService loanService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(LoanStatementRequestDto request) {
        return ResponseEntity.ok(loanService.prescoring(request));
    }

    @Override
    public ResponseEntity<StatementDto> selectOffer(LoanOfferDto request) {
        return ResponseEntity.ok(loanService.selectOffer(request));
    }
}
