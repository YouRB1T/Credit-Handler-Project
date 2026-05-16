package com.credithandler.deal.controller;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.service.StatementAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/deal/admin")
@RequiredArgsConstructor
public class DealAdminController implements com.credithandler.api.controller.DealAdminController {

    private final StatementAdminService statementAdminService;

    @Override
    public ResponseEntity<StatementDto> getStatementById(UUID statementId) {
        log.info(">> getStatementById, statementId: {}", statementId);

        StatementDto statement = statementAdminService.getStatementById(statementId);

        log.info("<< getStatementById, statementId: {}, status: {}", statementId, statement.getStatus());
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<List<StatementDto>> getAllStatements() {
        log.info(">> getAllStatements");

        List<StatementDto> statements = statementAdminService.getAllStatements();

        log.info("<< getAllStatements, count: {}", statements.size());
        return ResponseEntity.ok(statements);
    }
}
