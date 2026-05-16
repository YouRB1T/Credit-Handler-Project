package com.credithandler.gateway.controller;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.controller.api.GatewayAdminControllerApi;
import com.credithandler.gateway.service.GatewayDealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin/statement")
@RequiredArgsConstructor
public class GatewayAdminController implements GatewayAdminControllerApi {

    private final GatewayDealService gatewayDealService;

    @Override
    public ResponseEntity<StatementDto> getStatementById(@PathVariable UUID statementId) {
        log.info(">> getStatementById, statementId: {}", statementId);

        StatementDto statement = gatewayDealService.getStatementById(statementId);

        log.info("<< getStatementById, statementId: {}, status: {}", statementId, statement.getStatus());
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<List<StatementDto>> getAllStatements() {
        log.info(">> getAllStatements");

        List<StatementDto> statements = gatewayDealService.getAllStatements();

        log.info("<< getAllStatements, count: {}", statements.size());
        return ResponseEntity.ok(statements);
    }
}
