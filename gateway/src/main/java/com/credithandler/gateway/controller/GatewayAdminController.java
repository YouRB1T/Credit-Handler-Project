package com.credithandler.gateway.controller;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.service.GatewayDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/statement")
@RequiredArgsConstructor
public class GatewayAdminController {

    private final GatewayDealService gatewayDealService;

    @GetMapping("/{statementId}")
    public ResponseEntity<StatementDto> getStatementById(@PathVariable UUID statementId) {
        return ResponseEntity.ok(gatewayDealService.getStatementById(statementId));
    }

    @GetMapping
    public ResponseEntity<List<StatementDto>> getAllStatements() {
        return ResponseEntity.ok(gatewayDealService.getAllStatements());
    }
}
