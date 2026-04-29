package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String TEXT_CONTENT_TYPE = "text/plain";
    private static final String CONTRACT_BASE_NAME = "credit-contract";
    private static final String PAYMENT_SCHEDULE_BASE_NAME = "payment-schedule";
    private static final String INDIVIDUAL_CONDITIONS_BASE_NAME = "individual-conditions";

    @Value("${dossier.documents.storage-path}")
    private String storagePath;

    @Override
    public void createCreditDocuments(StatementDto statement) {
        log.info(">> createCreditDocuments, statementId: {}", statement.getStatementId());

        Path statementDirectory = getStatementDirectory(statement.getStatementId());
        try {
            Files.createDirectories(statementDirectory);
            writeDocument(statementDirectory, statement, CONTRACT_BASE_NAME, buildCreditContract(statement));
            writeDocument(statementDirectory, statement, PAYMENT_SCHEDULE_BASE_NAME, buildPaymentSchedule(statement));
            writeDocument(statementDirectory, statement, INDIVIDUAL_CONDITIONS_BASE_NAME, buildIndividualConditions(statement));
        } catch (IOException ex) {
            throw BusinessException.of(
                    "Ошибка формирования документов",
                    "Не удалось сформировать документы по заявке %s".formatted(statement.getStatementId())
            );
        }

        log.info("<< createCreditDocuments, statementId: {}", statement.getStatementId());
    }

    @Override
    public List<GeneratedDocument> getCreditDocuments(StatementDto statement) {
        log.info(">> getCreditDocuments, statementId: {}", statement.getStatementId());

        Path statementDirectory = getStatementDirectory(statement.getStatementId());
        if (!Files.exists(statementDirectory)) {
            throw BusinessException.of(
                    "Документы не найдены",
                    "Документы по заявке %s еще не сформированы".formatted(statement.getStatementId())
            );
        }

        List<GeneratedDocument> documents = List.of(
                readDocument(statementDirectory, statement, CONTRACT_BASE_NAME),
                readDocument(statementDirectory, statement, PAYMENT_SCHEDULE_BASE_NAME),
                readDocument(statementDirectory, statement, INDIVIDUAL_CONDITIONS_BASE_NAME)
        );

        log.info("<< getCreditDocuments, documents count: {}", documents.size());
        return documents;
    }

    private void writeDocument(Path statementDirectory, StatementDto statement, String baseName, String content)
            throws IOException {
        Files.writeString(
                statementDirectory.resolve(buildStoredFileName(baseName, statement.getStatementId())),
                content,
                StandardCharsets.UTF_8
        );
    }

    private GeneratedDocument readDocument(Path statementDirectory, StatementDto statement, String baseName) {
        String storedFileName = buildStoredFileName(baseName, statement.getStatementId());
        Path documentPath = statementDirectory.resolve(storedFileName);
        try {
            return new GeneratedDocument(
                    storedFileName,
                    buildAttachmentName(baseName),
                    TEXT_CONTENT_TYPE,
                    Files.readAllBytes(documentPath)
            );
        } catch (IOException ex) {
            throw BusinessException.of(
                    "Документ не найден",
                    "Не удалось прочитать документ %s по заявке %s".formatted(storedFileName, statement.getStatementId())
            );
        }
    }

    private Path getStatementDirectory(UUID statementId) {
        return Path.of(storagePath).resolve(statementId.toString());
    }

    private String buildStoredFileName(String baseName, UUID statementId) {
        return "%s-%s.txt".formatted(baseName, statementId);
    }

    private String buildAttachmentName(String baseName) {
        return "%s.txt".formatted(baseName);
    }

    private String buildCreditContract(StatementDto statement) {
        LoanOfferDto offer = statement.getAppliedOffer();
        return """
                Кредитный договор

                Номер заявки: %s
                Дата формирования: %s
                Идентификатор клиента: %s
                Идентификатор кредита: %s
                Статус заявки: %s

                Сумма кредита: %s
                Срок кредита: %s месяцев
                Процентная ставка: %s
                Ежемесячный платеж: %s

                Настоящий документ сформирован автоматически для кредитной заявки клиента.
                """.formatted(
                statement.getStatementId(),
                LocalDate.now(),
                statement.getClientId(),
                statement.getCreditId(),
                statement.getStatus(),
                offer != null ? offer.getRequestedAmount() : "не указано",
                offer != null ? offer.getTerm() : "не указано",
                offer != null ? offer.getRate() : "не указано",
                offer != null ? offer.getMonthlyPayment() : "не указано"
        );
    }

    private String buildPaymentSchedule(StatementDto statement) {
        LoanOfferDto offer = statement.getAppliedOffer();
        return """
                График платежей

                Номер заявки: %s
                Дата формирования: %s
                Срок кредита: %s месяцев
                Ежемесячный платеж: %s
                Полная сумма к возврату: %s

                Детальный график платежей будет сформирован на основании рассчитанного кредита.
                """.formatted(
                statement.getStatementId(),
                LocalDate.now(),
                offer != null ? offer.getTerm() : "не указано",
                offer != null ? offer.getMonthlyPayment() : "не указано",
                offer != null ? offer.getTotalAmount() : "не указано"
        );
    }

    private String buildIndividualConditions(StatementDto statement) {
        LoanOfferDto offer = statement.getAppliedOffer();
        return """
                Индивидуальные условия кредитования

                Номер заявки: %s
                Дата формирования: %s
                Страхование подключено: %s
                Зарплатный клиент: %s

                Индивидуальные условия сформированы на основании выбранного кредитного предложения.
                """.formatted(
                statement.getStatementId(),
                LocalDate.now(),
                offer != null ? offer.getIsInsuranceEnabled() : "не указано",
                offer != null ? offer.getIsSalaryClient() : "не указано"
        );
    }
}
