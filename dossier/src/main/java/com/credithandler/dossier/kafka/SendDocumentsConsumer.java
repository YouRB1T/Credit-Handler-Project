package com.credithandler.dossier.kafka;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.service.DossierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendDocumentsConsumer {

    private final DossierService dossierService;

    @KafkaListener(
            topics = "${kafka.topic.send-documents}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "statementDtoKafkaListenerContainerFactory"
    )
    public void consume(StatementDto statement) {
        log.info(">> consume send-documents, statement: {}", statement);

        dossierService.sendDocuments(statement);

        log.info("<< consume send-documents, statementId: {}", statement.getStatementId());
    }
}
