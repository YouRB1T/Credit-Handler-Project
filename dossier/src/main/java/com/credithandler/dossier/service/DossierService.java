package com.credithandler.dossier.service;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;

import java.util.UUID;

public interface DossierService {

    void sendDocuments(UUID statementId);

    StatementDto signDocuments(UUID statementId);

    StatementDto confirmSesCode(UUID statementId, SesCodeRequestDto request);
}
