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

    @Value("${kafka.topic.send-documents}")
    private String sendDocumentsTopic;

    @Override
    public void sendFinishRegistrationMessage(EmailMessage message) {
        log.info(">> sendFinishRegistrationMessage, message: {}", message);

        kafkaTemplate.send(finishRegistrationTopic, message.getStatementId().toString(), message);

        log.info("<< sendFinishRegistrationMessage, topic: {}, statementId: {}",
                finishRegistrationTopic, message.getStatementId());
    }

    @Override
    public void sendDocumentsMessage(StatementDto statement) {
        log.info(">> sendDocumentsMessage, statement: {}", statement);

        statementDtoKafkaTemplate.send(sendDocumentsTopic, statement.getStatementId().toString(), statement);

        log.info("<< sendDocumentsMessage, topic: {}, statementId: {}",
                sendDocumentsTopic, statement.getStatementId());
    }
}
