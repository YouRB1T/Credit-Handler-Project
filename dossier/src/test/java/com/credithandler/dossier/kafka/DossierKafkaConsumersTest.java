package com.credithandler.dossier.kafka;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.dossier.EmailTheme;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.service.DossierService;
import com.credithandler.dossier.service.EmailMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("Dossier Kafka consumers")
class DossierKafkaConsumersTest {

    private final EmailMessageService emailMessageService = mock(EmailMessageService.class);
    private final DossierService dossierService = mock(DossierService.class);

    @Test
    void createDocumentsConsumerDelegatesToEmailMessageService() {
        StatementDto statement = statement();

        new CreateDocumentsConsumer(emailMessageService).consume(statement);

        verify(emailMessageService).processCreateDocuments(statement);
    }

    @Test
    void creditIssuedConsumerDelegatesToEmailMessageService() {
        EmailMessage message = emailMessage(EmailTheme.CREDIT_ISSUED);

        new CreditIssuedConsumer(emailMessageService).consume(message);

        verify(emailMessageService).processCreditIssued(message);
    }

    @Test
    void finishRegistrationConsumerDelegatesToEmailMessageService() {
        EmailMessage message = emailMessage(EmailTheme.FINISH_REGISTRATION);

        new FinishRegistrationConsumer(emailMessageService).consume(message);

        verify(emailMessageService).processFinishRegistration(message);
    }

    @Test
    void sendDocumentsConsumerDelegatesToDossierService() {
        StatementDto statement = statement();

        new SendDocumentsConsumer(dossierService).consume(statement);

        verify(dossierService).sendDocuments(statement);
    }

    @Test
    void sendSesConsumerDelegatesToEmailMessageService() {
        EmailMessage message = emailMessage(EmailTheme.SEND_SES);

        new SendSesConsumer(emailMessageService).consume(message);

        verify(emailMessageService).processSendSes(message);
    }

    @Test
    void statementDeniedConsumerDelegatesToEmailMessageService() {
        EmailMessage message = emailMessage(EmailTheme.STATEMENT_DENIED);

        new StatementDeniedConsumer(emailMessageService).consume(message);

        verify(emailMessageService).processStatementDenied(message);
    }

    private static EmailMessage emailMessage(EmailTheme theme) {
        return new EmailMessage("client@example.com", theme, UUID.randomUUID(), "text");
    }

    private static StatementDto statement() {
        StatementDto statement = new StatementDto();
        statement.setStatementId(UUID.randomUUID());
        statement.setEmail("client@example.com");
        statement.setStatus(ApplicationStatus.CC_APPROVED);
        return statement;
    }
}
