package com.credithandler.dossier.service;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.config.DocumentProperties;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENTS_NOT_FOUND_ERROR;

@DisplayName("Тестирование DocumentServiceImpl")
class DocumentServiceImplTest {

    @TempDir
    private Path tempDir;

    @Test
    @DisplayName("createCreditDocuments создает три PDF документа")
    void createCreditDocuments_shouldCreateThreePdfDocuments() {
        DocumentServiceImpl documentService = new DocumentServiceImpl(documentProperties());
        StatementDto statement = statement();

        documentService.createCreditDocuments(statement);

        Path statementDirectory = tempDir.resolve(statement.getStatementId().toString());

        assertThat(statementDirectory)
                .exists()
                .isDirectory();
        assertThat(statementDirectory)
                .isDirectoryContaining(path -> path.getFileName().toString().startsWith("contract-"))
                .isDirectoryContaining(path -> path.getFileName().toString().startsWith("payment-schedule-"))
                .isDirectoryContaining(path -> path.getFileName().toString().startsWith("individual-conditions-"));
    }

    @Test
    @DisplayName("getCreditDocuments возвращает сгенерированные документы")
    void getCreditDocuments_shouldReturnGeneratedDocuments() {
        DocumentServiceImpl documentService = new DocumentServiceImpl(documentProperties());
        StatementDto statement = statement();

        documentService.createCreditDocuments(statement);
        List<GeneratedDocument> result = documentService.getCreditDocuments(statement);

        assertThat(result)
                .hasSize(3)
                .extracting(GeneratedDocument::contentType)
                .containsOnly("application/pdf");
        assertThat(result)
                .extracting(GeneratedDocument::attachmentName)
                .containsExactlyInAnyOrder("contract.pdf", "payment-schedule.pdf", "individual-conditions.pdf");
        assertThat(result)
                .allSatisfy(document -> assertThat(document.content()).isNotEmpty());
    }

    @Test
    @DisplayName("getCreditDocuments выбрасывает BusinessException если документы не найдены")
    void getCreditDocuments_whenDocumentsMissing_shouldThrowBusinessException() {
        DocumentServiceImpl documentService = new DocumentServiceImpl(documentProperties());

        assertThatThrownBy(() -> documentService.getCreditDocuments(statement()))
                .isInstanceOf(BusinessException.class)
                .extracting("message")
                .isEqualTo(DOCUMENTS_NOT_FOUND_ERROR);
    }

    private DocumentProperties documentProperties() {
        DocumentProperties properties = new DocumentProperties();
        properties.setStoragePath(tempDir.toString());
        properties.setPdfContentType("application/pdf");
        properties.setFileExtension(".pdf");
        properties.setContractBaseName("contract");
        properties.setPaymentScheduleBaseName("payment-schedule");
        properties.setIndividualConditionsBaseName("individual-conditions");

        DocumentProperties.FontProperties font = new DocumentProperties.FontProperties();
        font.setPath(String.join(",",
                tempDir.resolve("missing-windows-font.ttf").toString(),
                tempDir.resolve("missing-linux-font.ttf").toString(),
                tempDir.resolve("missing-alt-font.ttf").toString()
        ));
        properties.setFont(font);

        return properties;
    }

    private StatementDto statement() {
        StatementDto statement = new StatementDto();
        statement.setStatementId(UUID.randomUUID());
        statement.setClientId(UUID.randomUUID());
        statement.setCreditId(UUID.randomUUID());
        statement.setStatus(ApplicationStatus.CC_APPROVED);
        statement.setAppliedOffer(offer());
        return statement;
    }

    private LoanOfferDto offer() {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setRequestedAmount(new BigDecimal("100000"));
        offer.setTotalAmount(new BigDecimal("120000"));
        offer.setTerm(12);
        offer.setMonthlyPayment(new BigDecimal("10000"));
        offer.setRate(new BigDecimal("12.5"));
        offer.setIsInsuranceEnabled(true);
        offer.setIsSalaryClient(false);
        return offer;
    }
}
