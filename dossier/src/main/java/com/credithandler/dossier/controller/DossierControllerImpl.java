package com.credithandler.dossier.controller;

import com.credithandler.api.controller.DossierController;
import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.service.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/dossier")
@RequiredArgsConstructor
public class DossierControllerImpl implements DossierController {
    private final DossierService dossierService;

    @Override
    public ResponseEntity<Void> send(UUID statementId) {
        dossierService.sendDocuments(statementId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<StatementDto> sign(UUID statementId) {
        StatementDto statement = dossierService.signDocuments(statementId);
        return ResponseEntity.ok(statement);
    }

    @Override
    public ResponseEntity<StatementDto> code(SesCodeRequestDto request, UUID statementId) {
        StatementDto statement = dossierService.confirmSesCode(statementId, request);
        return ResponseEntity.ok(statement);
    }
}
