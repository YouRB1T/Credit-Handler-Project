package com.credithandler.gateway.controller;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.controller.api.GatewayDealControllerApi;
import com.credithandler.gateway.service.GatewayDealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GatewayDealController implements GatewayDealControllerApi {

    private final GatewayDealService gatewayDealService;

    @Override
    public ResponseEntity<StatementDto> registrationDealAndCountCredit(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @PathVariable UUID statementId) {
        log.info(">> registrationDealAndCountCredit, statementId: {}, request: {}", statementId, request);

        StatementDto statement = gatewayDealService.registrationDealAndCountCredit(request, statementId);

        log.info("<< registrationDealAndCountCredit, statementId: {}, status: {}", statementId, statement.getStatus());
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<StatementDto> sendDocuments(@PathVariable UUID statementId) {
        log.info(">> sendDocuments, statementId: {}", statementId);

        StatementDto statement = gatewayDealService.sendDocuments(statementId);

        log.info("<< sendDocuments, statementId: {}, status: {}", statementId, statement.getStatus());
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<StatementDto> signDocuments(@PathVariable UUID statementId) {
        log.info(">> signDocuments, statementId: {}", statementId);

        StatementDto statement = gatewayDealService.signDocuments(statementId);

        log.info("<< signDocuments, statementId: {}, status: {}", statementId, statement.getStatus());
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<StatementDto> codeDocuments(
            @Valid @RequestBody SesCodeRequestDto request,
            @PathVariable UUID statementId) {
        log.info(">> codeDocuments, statementId: {}", statementId);

        StatementDto statement = gatewayDealService.codeDocuments(statementId, request);

        log.info("<< codeDocuments, statementId: {}, status: {}", statementId, statement.getStatus());
        return ResponseEntity.ok(statement);
    }
}
