package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.EmailDetail;
import com.credithandler.dossier.model.EmailTemplateModel;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.model.RenderedEmail;
import com.credithandler.dossier.service.DocumentService;
import com.credithandler.dossier.service.EmailMessageService;
import com.credithandler.dossier.service.EmailTemplateService;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_MESSAGE_CREATION_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_MESSAGE_CREATION_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_RECIPIENT_NOT_REACHED_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_RECIPIENT_NOT_REACHED_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_SEND_ERROR;
import static com.credithandler.dossier.constants.EmailErrorConstants.EMAIL_SEND_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.EmailTextConstants.CREATE_DOCUMENTS_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.CREDIT_ISSUED_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.FINISH_REGISTRATION_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.SEND_DOCUMENTS_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.SEND_SES_SUBJECT;
import static com.credithandler.dossier.constants.EmailTextConstants.STATEMENT_DENIED_SUBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageServiceImpl implements EmailMessageService {

    private static final String DEFAULT_SECURITY_NOTE =
            "Мы никогда не просим отправлять коды подтверждения, пароли или паспортные данные ответным письмом.";

    private final JavaMailSender javaMailSender;
    private final DocumentService documentService;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.from}")
    private String mailFrom;

    @Override
    public void processFinishRegistration(EmailMessage message) {
        log.info(">> processFinishRegistration, message: {}", message);

        sendHtmlEmail(
                message.getAddress(),
                emailTemplateService.render(buildFinishRegistrationModel(message)),
                List.of()
        );

        log.info("<< processFinishRegistration, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processCreateDocuments(StatementDto statement) {
        log.info(">> processCreateDocuments, statement: {}", statement);

        documentService.createCreditDocuments(statement);
        sendHtmlEmail(
                statement.getEmail(),
                emailTemplateService.render(buildCreateDocumentsModel(statement)),
                List.of()
        );

        log.info("<< processCreateDocuments, documents created, email sent to: {}, statementId: {}",
                statement.getEmail(), statement.getStatementId());
    }

    @Override
    public void processSendDocuments(StatementDto statement) {
        log.info(">> processSendDocuments, statement: {}", statement);

        List<GeneratedDocument> documents = documentService.getCreditDocuments(statement);
        sendHtmlEmail(
                statement.getEmail(),
                emailTemplateService.render(buildSendDocumentsModel(statement, documents)),
                documents
        );

        log.info("<< processSendDocuments, email sent to: {}, statementId: {}, attachments count: {}",
                statement.getEmail(), statement.getStatementId(), documents.size());
    }

    @Override
    public void processSendSes(EmailMessage message) {
        log.info(">> processSendSes, message: {}", message);

        sendHtmlEmail(
                message.getAddress(),
                emailTemplateService.render(buildSendSesModel(message)),
                List.of()
        );

        log.info("<< processSendSes, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processCreditIssued(EmailMessage message) {
        log.info(">> processCreditIssued, message: {}", message);

        sendHtmlEmail(
                message.getAddress(),
                emailTemplateService.render(buildCreditIssuedModel(message)),
                List.of()
        );

        log.info("<< processCreditIssued, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    @Override
    public void processStatementDenied(EmailMessage message) {
        log.info(">> processStatementDenied, message: {}", message);

        sendHtmlEmail(
                message.getAddress(),
                emailTemplateService.render(buildStatementDeniedModel(message)),
                List.of()
        );

        log.info("<< processStatementDenied, email sent to: {}, statementId: {}",
                message.getAddress(), message.getStatementId());
    }

    private EmailTemplateModel buildFinishRegistrationModel(EmailMessage message) {
        return new EmailTemplateModel(
                FINISH_REGISTRATION_SUBJECT,
                "Заявка предварительно одобрена. Завершите регистрацию, чтобы продолжить оформление.",
                "Предварительное решение",
                "Заявка предварительно одобрена",
                "Мы предварительно одобрили вашу кредитную заявку. " + message.getText(),
                message.getStatementId(),
                null,
                List.of(new EmailDetail("Следующий этап", "Заполнение анкеты")),
                "Что дальше",
                "Завершите регистрацию и проверьте данные, чтобы мы могли перейти к финальному решению.",
                null,
                List.of(),
                DEFAULT_SECURITY_NOTE
        );
    }

    private EmailTemplateModel buildCreateDocumentsModel(StatementDto statement) {
        return new EmailTemplateModel(
                CREATE_DOCUMENTS_SUBJECT,
                "Кредитные документы сформированы и готовы к отправке.",
                "Документы",
                "Документы сформированы",
                "Мы подготовили комплект кредитных документов по вашей заявке.",
                statement.getStatementId(),
                getStatus(statement),
                List.of(new EmailDetail("Получатель", statement.getEmail())),
                "Следующий шаг",
                "Запросите отправку документов, чтобы ознакомиться с ними и перейти к подписанию.",
                null,
                List.of(),
                DEFAULT_SECURITY_NOTE
        );
    }

    private EmailTemplateModel buildSendDocumentsModel(StatementDto statement, List<GeneratedDocument> documents) {
        return new EmailTemplateModel(
                SEND_DOCUMENTS_SUBJECT,
                "Документы готовы и приложены к письму.",
                "Подписание",
                "Кредитные документы готовы к подписанию",
                "Во вложении находятся документы по вашей кредитной заявке. Проверьте их перед подписанием.",
                statement.getStatementId(),
                getStatus(statement),
                List.of(new EmailDetail("Количество файлов", String.valueOf(documents.size()))),
                "Что дальше",
                "Ознакомьтесь с PDF-файлами во вложении. После проверки переходите к подписанию документов.",
                null,
                documents.stream()
                        .map(GeneratedDocument::attachmentName)
                        .toList(),
                "Если вы не ожидаете эти документы, не открывайте вложения и обратитесь в поддержку."
        );
    }

    private EmailTemplateModel buildSendSesModel(EmailMessage message) {
        return new EmailTemplateModel(
                SEND_SES_SUBJECT,
                "Используйте код из письма для подписания кредитных документов.",
                "Код подтверждения",
                "Код для подписания документов",
                "Введите этот код в сервисе, чтобы подтвердить подписание кредитных документов.",
                message.getStatementId(),
                null,
                List.of(new EmailDetail("Срок действия", "Ограничен текущей операцией")),
                "Важно",
                "Не сообщайте код сотрудникам, знакомым или в ответных письмах. Код нужен только в сервисе.",
                message.getText(),
                List.of(),
                DEFAULT_SECURITY_NOTE
        );
    }

    private EmailTemplateModel buildCreditIssuedModel(EmailMessage message) {
        return new EmailTemplateModel(
                CREDIT_ISSUED_SUBJECT,
                "Кредит успешно выдан.",
                "Готово",
                "Кредит выдан",
                "Подписание завершено, и кредит по вашей заявке успешно выдан.",
                message.getStatementId(),
                null,
                List.of(),
                "Сохраните письмо",
                "Оно поможет быстро найти номер заявки, если понадобится обратиться в поддержку.",
                null,
                List.of(),
                DEFAULT_SECURITY_NOTE
        );
    }

    private EmailTemplateModel buildStatementDeniedModel(EmailMessage message) {
        return new EmailTemplateModel(
                STATEMENT_DENIED_SUBJECT,
                "По кредитной заявке принято отрицательное решение.",
                "Решение по заявке",
                "Заявка не одобрена",
                "По вашей кредитной заявке принято отрицательное решение. Это письмо фиксирует текущий результат рассмотрения.",
                message.getStatementId(),
                null,
                List.of(),
                "Что можно сделать",
                "Проверьте корректность данных и условия кредитования перед новой заявкой.",
                null,
                List.of(),
                DEFAULT_SECURITY_NOTE
        );
    }

    private void sendHtmlEmail(String recipient, RenderedEmail email, List<GeneratedDocument> documents) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(mailFrom);
            helper.setTo(recipient);
            helper.setSubject(email.subject());
            helper.setText(email.plainText(), email.htmlText());

            for (GeneratedDocument document : documents) {
                helper.addAttachment(
                        document.attachmentName(),
                        new ByteArrayResource(document.content()),
                        document.contentType()
                );
            }

            log.info(">> sendHtmlEmail, from: {}, to: {}, subject: {}, attachments count: {}",
                    mailFrom, recipient, email.subject(), documents.size());
            javaMailSender.send(mimeMessage);
            log.info("<< sendHtmlEmail, to: {}", recipient);
        } catch (MessagingException ex) {
            log.error("Email message creation failed, recipient: {}, error: {}",
                    recipient, ex.getMessage(), ex);
            throw BusinessException.of(
                    EMAIL_MESSAGE_CREATION_ERROR,
                    String.format(EMAIL_MESSAGE_CREATION_ERROR_DESCRIPTION, recipient)
            );
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

    private String getStatus(StatementDto statement) {
        return statement.getStatus() == null ? null : statement.getStatus().name();
    }
}
