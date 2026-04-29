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
public class CreditIssuedConsumer {

    private final EmailMessageService emailMessageService;

    @KafkaListener(
            topics = "${kafka.topic.credit-issued}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(EmailMessage message) {
        log.info(">> consume credit-issued, message: {}", message);

        emailMessageService.processCreditIssued(message);

        log.info("<< consume credit-issued, statementId: {}", message.getStatementId());
    }
}
