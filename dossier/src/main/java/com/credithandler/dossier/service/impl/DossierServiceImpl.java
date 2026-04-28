package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.service.DossierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class DossierServiceImpl implements DossierService {
    private final Map<UUID, String> sesCodes = new ConcurrentHashMap<>();

    @Override
    public void sendDocuments(UUID statementId) {
        log.info(">> sendDocuments, statementId: {}", statementId);


        log.info("<< sendDocuments, statementId: {}", statementId);
    }

    @Override
    public StatementDto signDocuments(UUID statementId) {
        log.info(">> signDocuments, statementId: {}", statementId);

        String sesCode = generateSesCode();
        sesCodes.put(statementId, sesCode);

        StatementDto statement = buildStatement(statementId, ApplicationStatus.DOCUMENT_CREATED);
        statement.setSesCode(sesCode);

        log.info("<< signDocuments, statement: {}", statement);
        return statement;
    }

    @Override
    public StatementDto confirmSesCode(UUID statementId, SesCodeRequestDto request) {
        log.info(">> confirmSesCode, statementId: {}, request sesCode: {}", statementId, maskCode(request.getSesCode()));

        String expectedCode = sesCodes.get(statementId);
        if (expectedCode == null) {
            log.info("confirmSesCode, SES-code was not generated or expired, statementId: {}", statementId);
            throw BusinessException.of(
                    "SES-код не найден",
                    "Для заявки не был сгенерирован SES-код или срок действия кода истек"
            );
        }

        if (!expectedCode.equals(request.getSesCode())) {
            log.info("confirmSesCode, invalid SES-code, statementId: {}", statementId);
            throw BusinessException.of(
                    "Некорректный SES-код",
                    "Переданный код подтверждения не совпадает с сохраненным кодом заявки"
            );
        }

        sesCodes.remove(statementId);

        StatementDto statement = buildStatement(statementId, ApplicationStatus.CREDIT_ISSUED);
        statement.setSignDate(LocalDateTime.now());

        log.info("<< confirmSesCode, statement: {}", statement);
        return statement;
    }

    private String generateSesCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
    }

    private StatementDto buildStatement(UUID statementId, ApplicationStatus status) {
        StatementDto statement = new StatementDto();
        statement.setStatementId(statementId);
        statement.setStatus(status);
        statement.setCreationDate(LocalDateTime.now());
        return statement;
    }

    private String maskCode(String code) {
        if (code == null || code.length() < 2) {
            return "****";
        }
        return code.substring(0, 2) + "**";
    }
}
