package com.credithandler.deal.service;

import com.credithandler.api.dto.dossier.EmailMessage;

public interface EmailMessageProducer {

    void sendFinishRegistrationMessage(EmailMessage message);
}
