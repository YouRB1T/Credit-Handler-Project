package com.credithandler.dossier.service;

import com.credithandler.api.dto.model.StatementDto;

public interface DossierService {

    void sendDocuments(StatementDto statement);
}
