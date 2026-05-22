package com.credithandler.dossier.service;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.dossier.EmailTheme;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.impl.EmailMessageServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static com.credithandler.dossier.constants.EmailTextConstants.CREATE_DOCUMENTS_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.CREDIT_ISSUED_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.FINISH_REGISTRATION_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.SEND_DOCUMENTS_SUBJECT;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование EmailMessageServiceImpl")
class EmailMessageServiceImplTest {

    private static final String MAIL_FROM = "bank@example.com";

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private EmailMessageServiceImpl emailMessageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailMessageService, "mailFrom", MAIL_FROM);
    }

    @Test
    @DisplayName("processFinishRegistration отправляет простое письмо")
    void processFinishRegistration_shouldSendSimpleEmail() {
        EmailMessage message = emailMessage();
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailMessageService.processFinishRegistration(message);

        verify(javaMailSender).send(mailCaptor.capture());

        assertThat(mailCaptor.getValue().getFrom()).isEqualTo(MAIL_FROM);
        assertThat(mailCaptor.getValue().getTo()).containsExactly(message.getAddress());
        assertThat(mailCaptor.getValue().getSubject()).isEqualTo(FINISH_REGISTRATION_SUBJECT);
        assertThat(mailCaptor.getValue().getText())
                .contains(message.getStatementId().toString())
                .contains(message.getText());
    }

    @Test
    @DisplayName("processCreateDocuments создает документы и отправляет письмо")
    void processCreateDocuments_shouldCreateDocumentsAndSendEmail() {
        StatementDto statement = statement();
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailMessageService.processCreateDocuments(statement);

        verify(documentService).createCreditDocuments(statement);
        verify(javaMailSender).send(mailCaptor.capture());

        assertThat(mailCaptor.getValue().getFrom()).isEqualTo(MAIL_FROM);
        assertThat(mailCaptor.getValue().getTo()).containsExactly(statement.getEmail());
        assertThat(mailCaptor.getValue().getSubject()).isEqualTo(CREATE_DOCUMENTS_SUBJECT);
        assertThat(mailCaptor.getValue().getText())
                .contains(statement.getStatementId().toString())
                .contains(statement.getStatus().name());
    }

    @Test
    @DisplayName("processSendDocuments отправляет письмо с вложениями")
    void processSendDocuments_shouldSendEmailWithAttachments() throws Exception {
        StatementDto statement = statement();
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        List<GeneratedDocument> documents = List.of(
                new GeneratedDocument("contract-id.pdf", "contract.pdf", "application/pdf", new byte[]{1, 2, 3})
        );

        when(documentService.getCreditDocuments(statement)).thenReturn(documents);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailMessageService.processSendDocuments(statement);

        verify(documentService).getCreditDocuments(statement);
        verify(javaMailSender).send(mimeMessage);

        assertThat(mimeMessage.getAllRecipients()).hasSize(1);
        assertThat(mimeMessage.getSubject()).isEqualTo(SEND_DOCUMENTS_SUBJECT);
    }

    @Test
    @DisplayName("processCreditIssued отправляет письмо о выдаче кредита")
    void processCreditIssued_shouldSendSimpleEmail() {
        EmailMessage message = emailMessage();
        ArgumentCaptor<SimpleMailMessage> mailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailMessageService.processCreditIssued(message);

        verify(javaMailSender).send(mailCaptor.capture());

        assertThat(mailCaptor.getValue().getTo()).containsExactly(message.getAddress());
        assertThat(mailCaptor.getValue().getSubject()).isEqualTo(CREDIT_ISSUED_SUBJECT);
        assertThat(mailCaptor.getValue().getText()).contains(message.getStatementId().toString());
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
