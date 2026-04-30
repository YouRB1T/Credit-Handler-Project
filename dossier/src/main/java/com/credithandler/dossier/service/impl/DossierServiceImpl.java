package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.model.UpdateStatementStatusRequestDto;
import com.credithandler.dossier.client.DealClient;
import com.credithandler.dossier.service.DossierService;
import com.credithandler.dossier.service.EmailMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DossierServiceImpl implements DossierService {

    private final EmailMessageService emailMessageService;
    private final DealClient dealClient;

    @Override
    public void sendDocuments(StatementDto statement) {
        log.info(">> sendDocuments, statement: {}", statement);

        validateStatement(statement);
        emailMessageService.processSendDocuments(statement);
        dealClient.updateStatementStatus(
                statement.getStatementId(),
                new UpdateStatementStatusRequestDto(ApplicationStatus.DOCUMENTS_CREATED)
        );

        log.info("<< sendDocuments, statementId: {}, email: {}, status: {}",
                statement.getStatementId(), statement.getEmail(), ApplicationStatus.DOCUMENTS_CREATED);
    }

    private void validateStatement(StatementDto statement) {
        if (statement == null) {
            throw BusinessException.of(
                    "Некорректное сообщение",
                    "StatementDto не должен быть null"
            );
        }
        if (statement.getStatementId() == null) {
            throw BusinessException.of(
                    "Некорректное сообщение",
                    "statementId обязателен для отправки документов"
            );
        }
        if (statement.getEmail() == null || statement.getEmail().isBlank()) {
            throw BusinessException.of(
                    "Некорректное сообщение",
                    "Email клиента обязателен для отправки документов"
            );
        }
    }
}
