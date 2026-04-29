package com.credithandler.dossier.kafka;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.dossier.service.EmailMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendSesConsumer {

    private final EmailMessageService emailMessageService;

    @KafkaListener(
            topics = "${kafka.topic.send-ses}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(EmailMessage message) {
        log.info(">> consume send-ses, statementId: {}", message.getStatementId());

        emailMessageService.processSendSes(message);

        log.info("<< consume send-ses, statementId: {}", message.getStatementId());
    }
}
