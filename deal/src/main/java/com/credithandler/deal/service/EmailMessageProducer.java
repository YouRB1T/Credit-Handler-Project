package com.credithandler.deal.service;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;

public interface EmailMessageProducer {

    void sendFinishRegistrationMessage(EmailMessage message);

    void sendDocumentsMessage(StatementDto statement);
}
