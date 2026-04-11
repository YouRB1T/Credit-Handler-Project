package com.credithandler.calculator.controller;

import com.credithandler.api.controller.CalculatorController;
import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.CalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
public class CalculatorControllerImpl implements CalculatorController {

    private final CalculatorService service;

    @Override
    public ResponseEntity<List<LoanOfferDto>> provisionLoanOffers(@Valid @RequestBody LoanStatementRequestDto request) {
        return ResponseEntity.ok(
                service.calculatingOffers(request)
        );
    }

    @Override
    public ResponseEntity<CreditDto> calculateCredit(@Valid @RequestBody ScoringDataDto request) {
        return ResponseEntity.ok(
                service.calculateCredit(request)
        );
    }
}