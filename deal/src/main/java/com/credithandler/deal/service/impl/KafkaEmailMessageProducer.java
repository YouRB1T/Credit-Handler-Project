package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.config.KafkaTopicsConfig;
import com.credithandler.deal.service.EmailMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEmailMessageProducer implements EmailMessageProducer {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private final KafkaTemplate<String, StatementDto> statementDtoKafkaTemplate;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void sendFinishRegistrationMessage(EmailMessage message) {
        log.info(">> sendFinishRegistrationMessage, message: {}", message);

        kafkaTemplate.send(kafkaTopicsConfig.getFinishRegistration(), message.getStatementId().toString(), message);

        log.info("<< sendFinishRegistrationMessage, topic: {}, statementId: {}",
                kafkaTopicsConfig.getFinishRegistration(), message.getStatementId());
    }

    @Override
    public void sendCreateDocumentsMessage(StatementDto statement) {
        sendStatementMessage(kafkaTopicsConfig.getCreateDocuments(), statement, "sendCreateDocumentsMessage");
    }

    @Override
    public void sendDocumentsMessage(StatementDto statement) {
        log.info(">> sendDocumentsMessage, statement: {}", statement);

        statementDtoKafkaTemplate.send(kafkaTopicsConfig.getSendDocuments(), statement.getStatementId().toString(), statement);

        log.info("<< sendDocumentsMessage, topic: {}, statementId: {}",
                kafkaTopicsConfig.getSendDocuments(), statement.getStatementId());
    }

    @Override
    public void sendSesMessage(EmailMessage message) {
        sendEmailMessage(kafkaTopicsConfig.getSendSes(), message, "sendSesMessage");
    }

    @Override
    public void sendCreditIssuedMessage(EmailMessage message) {
        sendEmailMessage(kafkaTopicsConfig.getCreditIssued(), message, "sendCreditIssuedMessage");
    }

    @Override
    public void sendStatementDeniedMessage(EmailMessage message) {
        sendEmailMessage(kafkaTopicsConfig.getStatementDenied(), message, "sendStatementDeniedMessage");
    }

    private void sendEmailMessage(String topic, EmailMessage message, String methodName) {
        log.info(">> {}, message: {}", methodName, message);

        kafkaTemplate.send(topic, message.getStatementId().toString(), message);

        log.info("<< {}, topic: {}, statementId: {}", methodName, topic, message.getStatementId());
    }

    private void sendStatementMessage(String topic, StatementDto statement, String methodName) {
        log.info(">> {}, statement: {}", methodName, statement);

        statementDtoKafkaTemplate.send(topic, statement.getStatementId().toString(), statement);

        log.info("<< {}, topic: {}, statementId: {}", methodName, topic, statement.getStatementId());
    }
}
