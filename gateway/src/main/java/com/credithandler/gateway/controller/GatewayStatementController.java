package com.credithandler.gateway.controller;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.service.GatewayStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statement")
@RequiredArgsConstructor
public class GatewayStatementController {

    private final GatewayStatementService gatewayStatementService;

    @PostMapping
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @Valid @RequestBody LoanStatementRequestDto request) {
        return ResponseEntity.ok(gatewayStatementService.createStatement(request));
    }

    @PostMapping("/select")
    public ResponseEntity<StatementDto> selectOffer(
            @Valid @RequestBody LoanOfferDto request) {
        return ResponseEntity.ok(gatewayStatementService.selectOffer(request));
    }
}
