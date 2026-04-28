package com.credithandler.dossier.service;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;

import java.util.List;

public interface DocumentService {

    List<GeneratedDocument> generateCreditDocuments(StatementDto statement);
}
