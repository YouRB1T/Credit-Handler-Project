package com.credithandler.dossier.service;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.dossier.EmailTheme;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.EmailTemplateModel;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.model.RenderedEmail;
import com.credithandler.dossier.service.impl.EmailMessageServiceImpl;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.mail.Session;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_RECIPIENT_NOT_REACHED_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_SEND_ERROR;
import static com.credithandler.dossier.constants.EmailTextConstants.CREATE_DOCUMENTS_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.CREDIT_ISSUED_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.FINISH_REGISTRATION_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.SEND_DOCUMENTS_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.SEND_SES_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.STATEMENT_DENIED_SUBJECT;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование EmailMessageServiceImpl")
class EmailMessageServiceImplTest {

    private static final String MAIL_FROM = "bank@example.com";

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private DocumentService documentService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private EmailMessageServiceImpl emailMessageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailMessageService, "mailFrom", MAIL_FROM);
    }

    @Test
    @DisplayName("processFinishRegistration отправляет HTML письмо")
    void processFinishRegistration_shouldSendHtmlEmail() throws Exception {
        EmailMessage message = emailMessage();
        MimeMessage mimeMessage = prepareMimeMessage(FINISH_REGISTRATION_SUBJECT);
        ArgumentCaptor<EmailTemplateModel> modelCaptor = ArgumentCaptor.forClass(EmailTemplateModel.class);

        emailMessageService.processFinishRegistration(message);

        verify(emailTemplateService).render(modelCaptor.capture());
        verify(javaMailSender).send(mimeMessage);

        assertThat(modelCaptor.getValue().statementId()).isEqualTo(message.getStatementId());
        assertThat(modelCaptor.getValue().lead()).contains(message.getText());
        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(FINISH_REGISTRATION_SUBJECT);
    }

    @Test
    @DisplayName("processCreateDocuments создает документы и отправляет HTML письмо")
    void processCreateDocuments_shouldCreateDocumentsAndSendHtmlEmail() throws Exception {
        StatementDto statement = statement();
        MimeMessage mimeMessage = prepareMimeMessage(CREATE_DOCUMENTS_SUBJECT);
        ArgumentCaptor<EmailTemplateModel> modelCaptor = ArgumentCaptor.forClass(EmailTemplateModel.class);

        emailMessageService.processCreateDocuments(statement);

        verify(documentService).createCreditDocuments(statement);
        verify(emailTemplateService).render(modelCaptor.capture());
        verify(javaMailSender).send(mimeMessage);

        assertThat(modelCaptor.getValue().statementId()).isEqualTo(statement.getStatementId());
        assertThat(modelCaptor.getValue().status()).isEqualTo(statement.getStatus().name());
        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(CREATE_DOCUMENTS_SUBJECT);
    }

    @Test
    @DisplayName("processSendDocuments отправляет письмо с вложениями")
    void processSendDocuments_shouldSendEmailWithAttachments() throws Exception {
        StatementDto statement = statement();
        MimeMessage mimeMessage = prepareMimeMessage(SEND_DOCUMENTS_SUBJECT);
        List<GeneratedDocument> documents = List.of(
                new GeneratedDocument("contract-id.pdf", "contract.pdf", "application/pdf", new byte[]{1, 2, 3})
        );

        when(documentService.getCreditDocuments(statement)).thenReturn(documents);

        emailMessageService.processSendDocuments(statement);

        verify(documentService).getCreditDocuments(statement);
        verify(emailTemplateService).render(any(EmailTemplateModel.class));
        verify(javaMailSender).send(mimeMessage);

        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(SEND_DOCUMENTS_SUBJECT);
    }

    @Test
    @DisplayName("processCreditIssued отправляет HTML письмо о выдаче кредита")
    void processCreditIssued_shouldSendHtmlEmail() throws Exception {
        EmailMessage message = emailMessage();
        MimeMessage mimeMessage = prepareMimeMessage(CREDIT_ISSUED_SUBJECT);

        emailMessageService.processCreditIssued(message);

        verify(emailTemplateService).render(any(EmailTemplateModel.class));
        verify(javaMailSender).send(mimeMessage);

        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(CREDIT_ISSUED_SUBJECT);
    }

    @Test
    @DisplayName("processSendSes sends HTML email")
    void processSendSes_shouldSendHtmlEmail() throws Exception {
        EmailMessage message = emailMessage();
        MimeMessage mimeMessage = prepareMimeMessage(SEND_SES_SUBJECT);
        ArgumentCaptor<EmailTemplateModel> modelCaptor = ArgumentCaptor.forClass(EmailTemplateModel.class);

        emailMessageService.processSendSes(message);

        verify(emailTemplateService).render(modelCaptor.capture());
        verify(javaMailSender).send(mimeMessage);

        assertThat(modelCaptor.getValue().code()).isEqualTo(message.getText());
        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(SEND_SES_SUBJECT);
    }

    @Test
    @DisplayName("processStatementDenied sends HTML email")
    void processStatementDenied_shouldSendHtmlEmail() throws Exception {
        EmailMessage message = emailMessage();
        MimeMessage mimeMessage = prepareMimeMessage(STATEMENT_DENIED_SUBJECT);

        emailMessageService.processStatementDenied(message);

        verify(emailTemplateService).render(any(EmailTemplateModel.class));
        verify(javaMailSender).send(mimeMessage);

        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(STATEMENT_DENIED_SUBJECT);
    }

    @Nested
    @DisplayName("mail sending failures")
    class MailSendingFailures {

        private final Logger serviceLogger = (Logger) LoggerFactory.getLogger(EmailMessageServiceImpl.class);
        private Level previousLevel;

        @BeforeEach
        void disableExpectedErrorLogging() {
            previousLevel = serviceLogger.getLevel();
            serviceLogger.setLevel(Level.OFF);
        }

        @AfterEach
        void restoreLogging() {
            serviceLogger.setLevel(previousLevel);
        }

        @Test
        void htmlEmailThrowsRecipientNotReachedWhenSendFailedIsCause() {
            EmailMessage message = emailMessage();
            prepareMimeMessage(CREDIT_ISSUED_SUBJECT);
            doThrow(new MailSendException("smtp failed", new SendFailedException("rejected")))
                    .when(javaMailSender)
                    .send(any(MimeMessage.class));

            assertThatThrownBy(() -> emailMessageService.processCreditIssued(message))
                    .isInstanceOf(com.credithandler.api.dto.error.BusinessException.class)
                    .hasMessage(EMAIL_RECIPIENT_NOT_REACHED_ERROR);
        }

        @Test
        void htmlEmailThrowsGenericSendErrorForOtherMailFailures() {
            EmailMessage message = emailMessage();
            prepareMimeMessage(CREDIT_ISSUED_SUBJECT);
            doThrow(new MailSendException("smtp failed"))
                    .when(javaMailSender)
                    .send(any(MimeMessage.class));

            assertThatThrownBy(() -> emailMessageService.processCreditIssued(message))
                    .isInstanceOf(com.credithandler.api.dto.error.BusinessException.class)
                    .hasMessage(EMAIL_SEND_ERROR);
        }
    }

    private MimeMessage prepareMimeMessage(String subject) {
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(emailTemplateService.render(any(EmailTemplateModel.class))).thenReturn(renderedEmail(subject));
        return mimeMessage;
    }

    private RenderedEmail renderedEmail(String subject) {
        return new RenderedEmail(subject, "Plain text", "<!DOCTYPE html><html><body>HTML text</body></html>");
    }

    private EmailMessage emailMessage() {
        return new EmailMessage(
                "client@example.com",
                EmailTheme.FINISH_REGISTRATION,
                UUID.randomUUID(),
                "Продолжите оформление заявки"
        );
    }

    private StatementDto statement() {
        StatementDto statement = new StatementDto();
        statement.setStatementId(UUID.randomUUID());
        statement.setEmail("client@example.com");
        statement.setStatus(ApplicationStatus.CC_APPROVED);
        return statement;
    }
}
