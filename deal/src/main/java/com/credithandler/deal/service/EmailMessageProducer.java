package com.credithandler.deal.service;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;

public interface EmailMessageProducer {

    void sendFinishRegistrationMessage(EmailMessage message);

    void sendCreateDocumentsMessage(StatementDto statement);

    void sendDocumentsMessage(StatementDto statement);

    void sendSesMessage(EmailMessage message);

    void sendCreditIssuedMessage(EmailMessage message);

    void sendStatementDeniedMessage(EmailMessage message);
}
