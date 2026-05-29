package com.credithandler.gateway.controller;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.controller.api.GatewayStatementControllerApi;
import com.credithandler.gateway.service.GatewayStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statement")
@RequiredArgsConstructor
public class GatewayStatementController implements GatewayStatementControllerApi {

    private final GatewayStatementService gatewayStatementService;

    @Override
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @Valid @RequestBody LoanStatementRequestDto request) {
        log.info(">> createStatement, request: {}", request);

        List<LoanOfferDto> offers = gatewayStatementService.createStatement(request);

        log.info("<< createStatement, offers count: {}", offers.size());
        return ResponseEntity.ok(offers);
    }

    @Override
    public ResponseEntity<StatementDto> selectOffer(
            @Valid @RequestBody LoanOfferDto request) {
        log.info(">> selectOffer, request: {}", request);

        StatementDto statement = gatewayStatementService.selectOffer(request);

        log.info("<< selectOffer, statementId: {}, status: {}", statement.getStatementId(), statement.getStatus());
        return ResponseEntity.ok(statement);
    }
}
