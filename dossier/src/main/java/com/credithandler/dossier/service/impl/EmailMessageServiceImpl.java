package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.DocumentService;
import com.credithandler.dossier.service.EmailMessageService;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_MESSAGE_CREATION_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_MESSAGE_CREATION_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_RECIPIENT_NOT_REACHED_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_RECIPIENT_NOT_REACHED_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_SEND_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_SEND_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.EmailTextConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageServiceImpl implements EmailMessageService {

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

        sendSimpleMailMessage(mailMessage, message.getAddress());

        log.info("<< processFinishRegistration, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processCreateDocuments(StatementDto statement) {
        log.info(">> processCreateDocuments, statement: {}", statement);

        documentService.createCreditDocuments(statement);
        sendCreateDocumentsEmail(statement);

        log.info("<< processCreateDocuments, documents created, email sent to: {}, statementId: {}",
                statement.getEmail(), statement.getStatementId());
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
                buildSimpleText(message, CREDIT_ISSUED_TEXT)
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
                buildSimpleText(message, STATEMENT_DENIED_TEXT)
        );

        log.info("<< processStatementDenied, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    private String buildFinishRegistrationText(EmailMessage message) {
        return FINISH_REGISTRATION_TEXT.formatted(message.getStatementId(), message.getText());
    }

    private void sendSimpleEmail(EmailMessage message, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(message.getAddress());
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        sendSimpleMailMessage(mailMessage, message.getAddress());
    }

    private String buildSimpleText(EmailMessage message, String body) {
        return SIMPLE_TEXT.formatted(body, message.getStatementId());
    }

    private void sendCreateDocumentsEmail(StatementDto statement) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(mailFrom);
        mailMessage.setTo(statement.getEmail());
        mailMessage.setSubject(CREATE_DOCUMENTS_SUBJECT);
        mailMessage.setText(buildCreateDocumentsText(statement));

        sendSimpleMailMessage(mailMessage, statement.getEmail());
    }

    private String buildCreateDocumentsText(StatementDto statement) {
        return CREATE_DOCUMENTS_TEXT.formatted(statement.getStatementId(), statement.getStatus());
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

            log.info(">> sendMimeMailMessage, from: {}, to: {}, attachments count: {}",
                    mailFrom, statement.getEmail(), documents.size());
            javaMailSender.send(mimeMessage);
            log.info("<< sendMimeMailMessage, to: {}", statement.getEmail());
        } catch (MessagingException ex) {
            log.error("Email message creation failed, recipient: {}, error: {}",
                    statement.getEmail(), ex.getMessage(), ex);
            throw BusinessException.of(
                    EMAIL_MESSAGE_CREATION_ERROR,
                    String.format(EMAIL_MESSAGE_CREATION_ERROR_DESCRIPTION, statement.getEmail())
            );
        } catch (MailException ex) {
            handleMailException(statement.getEmail(), ex);
        }
    }

    private void sendSimpleMailMessage(SimpleMailMessage mailMessage, String recipient) {
        try {
            log.info(">> sendSimpleMailMessage, from: {}, to: {}, subject: {}",
                    mailMessage.getFrom(), recipient, mailMessage.getSubject());
            javaMailSender.send(mailMessage);
            log.info("<< sendSimpleMailMessage, to: {}", recipient);
        } catch (MailException ex) {
            handleMailException(recipient, ex);
        }
    }

    private void handleMailException(String recipient, MailException ex) {
        log.error("Email sending failed, recipient: {}, error: {}", recipient, ex.getMessage(), ex);

        if (containsSendFailedException(ex)) {
            throw BusinessException.of(
                    EMAIL_RECIPIENT_NOT_REACHED_ERROR,
                    String.format(EMAIL_RECIPIENT_NOT_REACHED_ERROR_DESCRIPTION, recipient)
            );
        }

        throw BusinessException.of(
                EMAIL_SEND_ERROR,
                String.format(EMAIL_SEND_ERROR_DESCRIPTION, recipient)
        );
    }

    private boolean containsSendFailedException(Throwable exception) {
        if (exception instanceof MailSendException mailSendException) {
            for (Exception messageException : mailSendException.getMessageExceptions()) {
                if (containsSendFailedException(messageException)) {
                    return true;
                }
            }
        }

        Throwable current = exception;
        while (current != null) {
            if (current instanceof SendFailedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildSendDocumentsText(StatementDto statement) {
        return SEND_DOCUMENTS_TEXT.formatted(statement.getStatementId(), statement.getStatus());
    }
}
