package com.credithandler.dossier.service;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;

import java.util.List;

public interface DocumentService {

    void createCreditDocuments(StatementDto statement);

    List<GeneratedDocument> getCreditDocuments(StatementDto statement);
}
