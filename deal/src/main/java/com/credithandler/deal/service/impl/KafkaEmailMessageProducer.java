package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.service.EmailMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaEmailMessageProducer implements EmailMessageProducer {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;
    private final KafkaTemplate<String, StatementDto> statementDtoKafkaTemplate;

    @Value("${kafka.topic.finish-registration}")
    private String finishRegistrationTopic;

    @Value("${kafka.topic.create-documents}")
    private String createDocumentsTopic;

    @Value("${kafka.topic.send-documents}")
    private String sendDocumentsTopic;

    @Value("${kafka.topic.send-ses}")
    private String sendSesTopic;

    @Value("${kafka.topic.credit-issued}")
    private String creditIssuedTopic;

    @Value("${kafka.topic.statement-denied}")
    private String statementDeniedTopic;

    @Override
    public void sendFinishRegistrationMessage(EmailMessage message) {
        log.info(">> sendFinishRegistrationMessage, message: {}", message);

        kafkaTemplate.send(finishRegistrationTopic, message.getStatementId().toString(), message);

        log.info("<< sendFinishRegistrationMessage, topic: {}, statementId: {}",
                finishRegistrationTopic, message.getStatementId());
    }

    @Override
    public void sendCreateDocumentsMessage(StatementDto statement) {
        sendStatementMessage(createDocumentsTopic, statement, "sendCreateDocumentsMessage");
    }

    @Override
    public void sendDocumentsMessage(StatementDto statement) {
        log.info(">> sendDocumentsMessage, statement: {}", statement);

        statementDtoKafkaTemplate.send(sendDocumentsTopic, statement.getStatementId().toString(), statement);

        log.info("<< sendDocumentsMessage, topic: {}, statementId: {}",
                sendDocumentsTopic, statement.getStatementId());
    }

    @Override
    public void sendSesMessage(EmailMessage message) {
        sendEmailMessage(sendSesTopic, message, "sendSesMessage");
    }

    @Override
    public void sendCreditIssuedMessage(EmailMessage message) {
        sendEmailMessage(creditIssuedTopic, message, "sendCreditIssuedMessage");
    }

    @Override
    public void sendStatementDeniedMessage(EmailMessage message) {
        sendEmailMessage(statementDeniedTopic, message, "sendStatementDeniedMessage");
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
