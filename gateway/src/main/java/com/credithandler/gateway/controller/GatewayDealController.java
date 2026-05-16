package com.credithandler.gateway.controller;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.service.GatewayDealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GatewayDealController {

    private final GatewayDealService gatewayDealService;

    @PostMapping("/statement/registration/{statementId}")
    public ResponseEntity<StatementDto> registrationDealAndCountCredit(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @PathVariable UUID statementId) {
        return ResponseEntity.ok(gatewayDealService.registrationDealAndCountCredit(request, statementId));
    }

    @PostMapping("/document/{statementId}")
    public ResponseEntity<StatementDto> sendDocuments(@PathVariable UUID statementId) {
        return ResponseEntity.ok(gatewayDealService.sendDocuments(statementId));
    }

    @PostMapping("/document/{statementId}/sign")
    public ResponseEntity<StatementDto> signDocuments(@PathVariable UUID statementId) {
        return ResponseEntity.ok(gatewayDealService.signDocuments(statementId));
    }

    @PostMapping("/document/{statementId}/sign/code")
    public ResponseEntity<StatementDto> codeDocuments(
            @Valid @RequestBody SesCodeRequestDto request,
            @PathVariable UUID statementId) {
        return ResponseEntity.ok(gatewayDealService.codeDocuments(statementId, request));
    }
}
