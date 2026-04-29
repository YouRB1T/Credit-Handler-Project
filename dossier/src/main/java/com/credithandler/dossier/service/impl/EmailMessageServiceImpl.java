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
    private static final String CREATE_DOCUMENTS_SUBJECT = "Формирование кредитных документов";
    private static final String SEND_DOCUMENTS_SUBJECT = "Кредитные документы по вашей заявке";
    private static final String SEND_SES_SUBJECT = "Код подтверждения подписания документов";
    private static final String CREDIT_ISSUED_SUBJECT = "Кредит выдан";
    private static final String STATEMENT_DENIED_SUBJECT = "Отказ по кредитной заявке";

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
    public void processCreateDocuments(StatementDto statement) {
        log.info(">> processCreateDocuments, statement: {}", statement);

        documentService.createCreditDocuments(statement);

        log.info("<< processCreateDocuments, documents created, statementId: {}", statement.getStatementId());
    }

    @Override
    public void processSendDocuments(StatementDto statement) {
        log.info(">> processSendDocuments, statement: {}", statement);

        List<GeneratedDocument> documents = documentService.getCreditDocuments(statement);
        sendDocumentsEmail(statement, documents);

        log.info("<< processSendDocuments, email sent to: {}, statementId: {}, attachments count: {}",
                statement.getEmail(), statement.getStatementId(), documents.size());
    }

    @Override
    public void processSendSes(EmailMessage message) {
        log.info(">> processSendSes, message: {}", message);

        sendSimpleEmail(
                message,
                SEND_SES_SUBJECT,
                buildSimpleText(message, message.getText())
        );

        log.info("<< processSendSes, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processCreditIssued(EmailMessage message) {
        log.info(">> processCreditIssued, message: {}", message);

        sendSimpleEmail(
                message,
                CREDIT_ISSUED_SUBJECT,
                buildSimpleText(message, "Поздравляем, кредит успешно выдан.")
        );

        log.info("<< processCreditIssued, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processStatementDenied(EmailMessage message) {
        log.info(">> processStatementDenied, message: {}", message);

        sendSimpleEmail(
                message,
                STATEMENT_DENIED_SUBJECT,
                buildSimpleText(message, "По вашей кредитной заявке принято отрицательное решение.")
        );

        log.info("<< processStatementDenied, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
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

    private void sendSimpleEmail(EmailMessage message, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(message.getAddress());
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        javaMailSender.send(mailMessage);
    }

    private String buildSimpleText(EmailMessage message, String body) {
        return """
                Здравствуйте!

                %s

                Номер заявки: %s
                """.formatted(body, message.getStatementId());
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
                        document.attachmentName(),
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
