package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.dossier.service.EmailMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageServiceImpl implements EmailMessageService {

    private static final String FINISH_REGISTRATION_SUBJECT = "Завершение регистрации кредитной заявки";

    private final JavaMailSender javaMailSender;

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

    private String buildFinishRegistrationText(EmailMessage message) {
        return """
                Здравствуйте!

                Ваша кредитная заявка предварительно одобрена.
                Для продолжения оформления кредита завершите регистрацию и заполните дополнительные данные.

                Номер заявки: %s

                %s
                """.formatted(message.getStatementId(), message.getText());
    }
}
