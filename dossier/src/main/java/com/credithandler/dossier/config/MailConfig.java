package com.credithandler.dossier.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final MailProperties mailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());

        if (mailProperties.getUsername() != null && !mailProperties.getUsername().isBlank()) {
            mailSender.setUsername(mailProperties.getUsername());
        }
        if (mailProperties.getPassword() != null && !mailProperties.getPassword().isBlank()) {
            mailSender.setPassword(mailProperties.getPassword());
        }

        mailSender.getJavaMailProperties().putAll(mailProperties.getProperties());

        return mailSender;
    }
}
