package com.credithandler.dossier.service;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;

public interface EmailMessageService {

    void processFinishRegistration(EmailMessage message);

    void processSendDocuments(StatementDto statement);
}
