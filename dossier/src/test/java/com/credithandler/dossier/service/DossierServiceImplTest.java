package com.credithandler.dossier.service;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.model.UpdateStatementStatusRequestDto;
import com.credithandler.dossier.client.DealClient;
import com.credithandler.dossier.service.impl.DossierServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static com.credithandler.dossier.constants.DossierServiceTextConstants.INVALID_MESSAGE_ERROR;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование DossierServiceImpl")
class DossierServiceImplTest {

    @Mock
    private EmailMessageService emailMessageService;

    @Mock
    private DealClient dealClient;

    @InjectMocks
    private DossierServiceImpl dossierService;

    @Test
    @DisplayName("sendDocuments отправляет документы и обновляет статус заявки")
    void sendDocuments_shouldSendDocumentsAndUpdateStatementStatus() {
        StatementDto statement = statement();
        ArgumentCaptor<UpdateStatementStatusRequestDto> requestCaptor =
                ArgumentCaptor.forClass(UpdateStatementStatusRequestDto.class);

        dossierService.sendDocuments(statement);

        verify(emailMessageService).processSendDocuments(statement);
        verify(dealClient).updateStatementStatus(
                org.mockito.ArgumentMatchers.eq(statement.getStatementId()),
                requestCaptor.capture()
        );

        assertThat(requestCaptor.getValue().getStatus())
                .isEqualTo(ApplicationStatus.DOCUMENTS_CREATED);
    }

    @Test
    @DisplayName("sendDocuments выбрасывает BusinessException если statement null")
    void sendDocuments_whenStatementNull_shouldThrowBusinessException() {
        assertThatThrownBy(() -> dossierService.sendDocuments(null))
                .isInstanceOf(BusinessException.class)
                .extracting("message")
                .isEqualTo(INVALID_MESSAGE_ERROR);

        verifyNoInteractions(emailMessageService, dealClient);
    }

    @Test
    @DisplayName("sendDocuments выбрасывает BusinessException если statementId null")
    void sendDocuments_whenStatementIdNull_shouldThrowBusinessException() {
        StatementDto statement = statement();
        statement.setStatementId(null);

        assertThatThrownBy(() -> dossierService.sendDocuments(statement))
                .isInstanceOf(BusinessException.class)
                .extracting("message")
                .isEqualTo(INVALID_MESSAGE_ERROR);

        verify(emailMessageService, never()).processSendDocuments(statement);
        verifyNoInteractions(dealClient);
    }

    @Test
    @DisplayName("sendDocuments выбрасывает BusinessException если email пустой")
    void sendDocuments_whenEmailBlank_shouldThrowBusinessException() {
        StatementDto statement = statement();
        statement.setEmail(" ");

        assertThatThrownBy(() -> dossierService.sendDocuments(statement))
                .isInstanceOf(BusinessException.class)
                .extracting("message")
                .isEqualTo(INVALID_MESSAGE_ERROR);

        verify(emailMessageService, never()).processSendDocuments(statement);
        verifyNoInteractions(dealClient);
    }

    private StatementDto statement() {
        StatementDto statement = new StatementDto();
        statement.setStatementId(UUID.randomUUID());
        statement.setEmail("client@example.com");
        return statement;
    }
}
