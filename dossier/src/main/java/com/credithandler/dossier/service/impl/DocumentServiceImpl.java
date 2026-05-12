package com.credithandler.dossier.service.impl;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.dossier.config.DocumentProperties;
import com.credithandler.dossier.model.GeneratedDocument;
import com.credithandler.dossier.service.DocumentService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENTS_NOT_FOUND_ERROR;
import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENTS_NOT_FOUND_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENT_CREATION_ERROR;
import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENT_CREATION_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENT_NOT_FOUND_ERROR;
import static com.credithandler.dossier.constants.DocumentErrorConstants.DOCUMENT_NOT_FOUND_ERROR_DESCRIPTION;
import static com.credithandler.dossier.constants.DocumentErrorConstants.NOT_SPECIFIED;
import static com.credithandler.dossier.constants.DocumentTextConstants.CLIENT_ID;
import static com.credithandler.dossier.constants.DocumentTextConstants.CREATED_AT;
import static com.credithandler.dossier.constants.DocumentTextConstants.CREDIT_CONTRACT_FOOTER;
import static com.credithandler.dossier.constants.DocumentTextConstants.CREDIT_CONTRACT_TITLE;
import static com.credithandler.dossier.constants.DocumentTextConstants.CREDIT_ID;
import static com.credithandler.dossier.constants.DocumentTextConstants.CREDIT_TERM;
import static com.credithandler.dossier.constants.DocumentTextConstants.INDIVIDUAL_CONDITIONS_FOOTER;
import static com.credithandler.dossier.constants.DocumentTextConstants.INDIVIDUAL_CONDITIONS_TITLE;
import static com.credithandler.dossier.constants.DocumentTextConstants.INSURANCE_ENABLED;
import static com.credithandler.dossier.constants.DocumentTextConstants.INTEREST_RATE;
import static com.credithandler.dossier.constants.DocumentTextConstants.MONTHLY_PAYMENT;
import static com.credithandler.dossier.constants.DocumentTextConstants.PAYMENT_SCHEDULE_FOOTER;
import static com.credithandler.dossier.constants.DocumentTextConstants.PAYMENT_SCHEDULE_TITLE;
import static com.credithandler.dossier.constants.DocumentTextConstants.REQUESTED_AMOUNT;
import static com.credithandler.dossier.constants.DocumentTextConstants.SALARY_CLIENT;
import static com.credithandler.dossier.constants.DocumentTextConstants.STATEMENT_ID;
import static com.credithandler.dossier.constants.DocumentTextConstants.STATEMENT_STATUS;
import static com.credithandler.dossier.constants.DocumentTextConstants.TOTAL_AMOUNT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentProperties documentProperties;

    @Override
    public void createCreditDocuments(StatementDto statement) {
        log.info(">> createCreditDocuments, statementId: {}", statement.getStatementId());

        Path statementDirectory = getStatementDirectory(statement.getStatementId());
        try {
            Files.createDirectories(statementDirectory);
            writeDocument(statementDirectory, statement, documentProperties.getContractBaseName(), buildCreditContract(statement));
            writeDocument(statementDirectory, statement, documentProperties.getPaymentScheduleBaseName(), buildPaymentSchedule(statement));
            writeDocument(statementDirectory, statement, documentProperties.getIndividualConditionsBaseName(), buildIndividualConditions(statement));
        } catch (IOException | DocumentException ex) {
            throw BusinessException.of(
                    DOCUMENT_CREATION_ERROR,
                    DOCUMENT_CREATION_ERROR_DESCRIPTION.formatted(statement.getStatementId())
            );
        }

        log.info("<< createCreditDocuments, statementId: {}, directory: {}",
                statement.getStatementId(), statementDirectory.toAbsolutePath().normalize());
    }

    @Override
    public List<GeneratedDocument> getCreditDocuments(StatementDto statement) {
        log.info(">> getCreditDocuments, statementId: {}", statement.getStatementId());

        Path statementDirectory = getStatementDirectory(statement.getStatementId());
        if (!Files.exists(statementDirectory)) {
            throw BusinessException.of(
                    DOCUMENTS_NOT_FOUND_ERROR,
                    DOCUMENTS_NOT_FOUND_ERROR_DESCRIPTION.formatted(statement.getStatementId())
            );
        }

        List<GeneratedDocument> documents = List.of(
                readDocument(statementDirectory, statement, documentProperties.getContractBaseName()),
                readDocument(statementDirectory, statement, documentProperties.getPaymentScheduleBaseName()),
                readDocument(statementDirectory, statement, documentProperties.getIndividualConditionsBaseName())
        );

        log.info("<< getCreditDocuments, documents count: {}, directory: {}",
                documents.size(), statementDirectory.toAbsolutePath().normalize());
        return documents;
    }

    private void writeDocument(Path statementDirectory, StatementDto statement, String baseName, List<String> lines)
            throws IOException, DocumentException {
        Files.write(
                statementDirectory.resolve(buildStoredFileName(baseName, statement.getStatementId())),
                buildPdf(lines)
        );
    }

    private GeneratedDocument readDocument(Path statementDirectory, StatementDto statement, String baseName) {
        String storedFileName = buildStoredFileName(baseName, statement.getStatementId());
        Path documentPath = statementDirectory.resolve(storedFileName);
        try {
            return new GeneratedDocument(
                    storedFileName,
                    buildAttachmentName(baseName),
                    documentProperties.getPdfContentType(),
                    Files.readAllBytes(documentPath)
            );
        } catch (IOException ex) {
            throw BusinessException.of(
                    DOCUMENT_NOT_FOUND_ERROR,
                    DOCUMENT_NOT_FOUND_ERROR_DESCRIPTION.formatted(storedFileName, statement.getStatementId())
            );
        }
    }

    private byte[] buildPdf(List<String> lines) throws IOException, DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        Font titleFont = createFont(16, Font.BOLD);
        Font textFont = createFont(12, Font.NORMAL);

        document.open();
        for (int i = 0; i < lines.size(); i++) {
            Font font = i == 0 ? titleFont : textFont;
            document.add(new Paragraph(lines.get(i), font));
        }
        document.close();

        return outputStream.toByteArray();
    }

    private Font createFont(int size, int style) throws IOException, DocumentException {
        Path fontPath = findFontPath();
        if (fontPath == null) {
            return FontFactory.getFont(FontFactory.HELVETICA, size, style);
        }

        BaseFont baseFont = BaseFont.createFont(
                fontPath.toString(),
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );
        return new Font(baseFont, size, style);
    }

    private Path findFontPath() {
        DocumentProperties.FontProperties font = documentProperties.getFont();
        return List.of(
                        Path.of(font.getWindowsArialPath()),
                        Path.of(font.getLinuxDejavuPath()),
                        Path.of(font.getLinuxDejavuAltPath())
                )
                .stream()
                .filter(Files::exists)
                .findFirst()
                .orElse(null);
    }

    private Path getStatementDirectory(UUID statementId) {
        return Path.of(documentProperties.getStoragePath()).resolve(statementId.toString());
    }

    private String buildStoredFileName(String baseName, UUID statementId) {
        return "%s-%s%s".formatted(baseName, statementId, documentProperties.getFileExtension());
    }

    private String buildAttachmentName(String baseName) {
        return "%s%s".formatted(baseName, documentProperties.getFileExtension());
    }

    private List<String> buildCreditContract(StatementDto statement) {
        LoanOfferDto offer = statement.getAppliedOffer();
        return List.of(
                CREDIT_CONTRACT_TITLE,
                STATEMENT_ID.formatted(statement.getStatementId()),
                CREATED_AT.formatted(LocalDate.now()),
                CLIENT_ID.formatted(statement.getClientId()),
                CREDIT_ID.formatted(statement.getCreditId()),
                STATEMENT_STATUS.formatted(statement.getStatus()),
                REQUESTED_AMOUNT.formatted(offer != null ? offer.getRequestedAmount() : NOT_SPECIFIED),
                CREDIT_TERM.formatted(offer != null ? offer.getTerm() : NOT_SPECIFIED),
                INTEREST_RATE.formatted(offer != null ? offer.getRate() : NOT_SPECIFIED),
                MONTHLY_PAYMENT.formatted(offer != null ? offer.getMonthlyPayment() : NOT_SPECIFIED),
                CREDIT_CONTRACT_FOOTER
        );
    }

    private List<String> buildPaymentSchedule(StatementDto statement) {
        LoanOfferDto offer = statement.getAppliedOffer();
        return List.of(
                PAYMENT_SCHEDULE_TITLE,
                STATEMENT_ID.formatted(statement.getStatementId()),
                CREATED_AT.formatted(LocalDate.now()),
                CREDIT_TERM.formatted(offer != null ? offer.getTerm() : NOT_SPECIFIED),
                MONTHLY_PAYMENT.formatted(offer != null ? offer.getMonthlyPayment() : NOT_SPECIFIED),
                TOTAL_AMOUNT.formatted(offer != null ? offer.getTotalAmount() : NOT_SPECIFIED),
                PAYMENT_SCHEDULE_FOOTER
        );
    }

    private List<String> buildIndividualConditions(StatementDto statement) {
        LoanOfferDto offer = statement.getAppliedOffer();
        return List.of(
                INDIVIDUAL_CONDITIONS_TITLE,
                STATEMENT_ID.formatted(statement.getStatementId()),
                CREATED_AT.formatted(LocalDate.now()),
                INSURANCE_ENABLED.formatted(offer != null ? offer.getIsInsuranceEnabled() : NOT_SPECIFIED),
                SALARY_CLIENT.formatted(offer != null ? offer.getIsSalaryClient() : NOT_SPECIFIED),
                INDIVIDUAL_CONDITIONS_FOOTER
        );
    }
}
