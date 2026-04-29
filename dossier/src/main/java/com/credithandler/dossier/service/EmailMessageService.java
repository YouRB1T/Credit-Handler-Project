package com.credithandler.dossier.service;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;

public interface EmailMessageService {

    void processFinishRegistration(EmailMessage message);

    void processCreateDocuments(StatementDto statement);

    void processSendDocuments(StatementDto statement);

    void processSendSes(EmailMessage message);

    void processCreditIssued(EmailMessage message);

    void processStatementDenied(EmailMessage message);
}
