package com.credithandler.dossier.service;

import com.credithandler.api.dto.dossier.EmailMessage;

public interface EmailMessageService {

    void processFinishRegistration(EmailMessage message);
}
