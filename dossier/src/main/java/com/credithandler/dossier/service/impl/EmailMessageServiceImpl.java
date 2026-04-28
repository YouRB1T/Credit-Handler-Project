package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.DocumentService;
import com.credithandler.dossier.service.EmailMessageService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageServiceImpl implements EmailMessageService {

    private static final String FINISH_REGISTRATION_SUBJECT = "Завершение регистрации кредитной заявки";
    private static final String SEND_DOCUMENTS_SUBJECT = "Кредитные документы по вашей заявке";

    private final JavaMailSender javaMailSender;
    private final DocumentService documentService;

    @Value("${spring.mail.from}")
    private String mailFrom;

    @Override
    public void processFinishRegistration(EmailMessage message) {
        log.info(">> processFinishRegistration, message: {}", message);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(message.getAddress());
        mailMessage.setSubject(FINISH_REGISTRATION_SUBJECT);
        mailMessage.setText(buildFinishRegistrationText(message));

        javaMailSender.send(mailMessage);

        log.info("<< processFinishRegistration, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processSendDocuments(StatementDto statement) {
        log.info(">> processSendDocuments, statement: {}", statement);

        List<GeneratedDocument> documents = documentService.generateCreditDocuments(statement);
        sendDocumentsEmail(statement, documents);

        log.info("<< processSendDocuments, email sent to: {}, statementId: {}, attachments count: {}",
                statement.getEmail(), statement.getStatementId(), documents.size());
    }

    private String buildFinishRegistrationText(EmailMessage message) {
        return """
                Здравствуйте!

                Ваша кредитная заявка предварительно одобрена.
                Для продолжения оформления кредита завершите регистрацию и заполните дополнительные данные.

                Номер заявки: %s

                %s
                """.formatted(message.getStatementId(), message.getText());
    }

    private void sendDocumentsEmail(StatementDto statement, List<GeneratedDocument> documents) {
        try {
            var mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(statement.getEmail());
            helper.setSubject(SEND_DOCUMENTS_SUBJECT);
            helper.setText(buildSendDocumentsText(statement), false);

            for (GeneratedDocument document : documents) {
                helper.addAttachment(
                        document.fileName(),
                        new ByteArrayResource(document.content()),
                        document.contentType()
                );
            }

            javaMailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Не удалось сформировать письмо с кредитными документами", ex);
        }
    }

    private String buildSendDocumentsText(StatementDto statement) {
        return """
                Здравствуйте!

                Кредитные документы по вашей заявке сформированы и приложены к письму.
                Ознакомьтесь с документами и перейдите к подписанию.

                Номер заявки: %s

                Статус заявки: %s
                """.formatted(statement.getStatementId(), statement.getStatus());
    }
}
