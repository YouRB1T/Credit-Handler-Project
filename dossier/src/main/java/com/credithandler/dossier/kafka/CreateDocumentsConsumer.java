package com.credithandler.dossier.kafka;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.service.EmailMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateDocumentsConsumer {

    private final EmailMessageService emailMessageService;

    @KafkaListener(
            topics = "${kafka.topic.create-documents}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "statementDtoKafkaListenerContainerFactory"
    )
    public void consume(StatementDto statement) {
        log.info(">> consume create-documents, statement: {}", statement);

        emailMessageService.processCreateDocuments(statement);

        log.info("<< consume create-documents, statementId: {}", statement.getStatementId());
    }
}
