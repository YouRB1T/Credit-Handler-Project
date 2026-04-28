package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String TEXT_CONTENT_TYPE = "text/plain";

    @Override
    public List<GeneratedDocument> generateCreditDocuments(StatementDto statement) {
        log.info(">> generateCreditDocuments, statementId: {}", statement.getStatementId());

        List<GeneratedDocument> documents = List.of(
                createDocument(
                        "credit-contract-%s.txt".formatted(statement.getStatementId()),
                        buildCreditContract(statement)
                ),
                createDocument(
                        "payment-schedule-%s.txt".formatted(statement.getStatementId()),
                        buildPaymentSchedule(statement)
                ),
                createDocument(
                        "individual-conditions-%s.txt".formatted(statement.getStatementId()),
                        buildIndividualConditions(statement)
                )
        );

        log.info("<< generateCreditDocuments, documents count: {}", documents.size());
        return documents;
    }

    private GeneratedDocument createDocument(String fileName, String content) {
        return new GeneratedDocument(
                fileName,
                TEXT_CONTENT_TYPE,
                content.getBytes(StandardCharsets.UTF_8)
        );
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
