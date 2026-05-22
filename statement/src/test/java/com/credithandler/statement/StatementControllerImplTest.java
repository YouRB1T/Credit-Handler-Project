package com.credithandler.statement;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.statement.controller.StatementControllerImpl;
import com.credithandler.statement.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование контроллера statement")
class StatementControllerImplTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private StatementControllerImpl statementController;

    @Test
    @DisplayName("Создание заявки возвращает кредитные предложения")
    void createStatementShouldReturnLoanOffers() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();

        LoanOfferDto offer1 = new LoanOfferDto();
        LoanOfferDto offer2 = new LoanOfferDto();
        List<LoanOfferDto> expectedOffers = List.of(offer1, offer2);

        when(loanService.prescoring(request)).thenReturn(expectedOffers);

        ResponseEntity<List<LoanOfferDto>> response =
                statementController.createStatement(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedOffers, response.getBody());

        verify(loanService).prescoring(request);
        verifyNoMoreInteractions(loanService);
    }

    @Test
    @DisplayName("Выбор предложения возвращает заявку")
    void selectOfferShouldReturnStatementDto() {
        LoanOfferDto request = new LoanOfferDto();

        StatementDto expectedStatement = new StatementDto();
        UUID statementId = UUID.randomUUID();
        expectedStatement.setStatementId(statementId);

        when(loanService.selectOffer(request)).thenReturn(expectedStatement);

        ResponseEntity<StatementDto> response =
                statementController.selectOffer(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedStatement, response.getBody());
        assertNotNull(response.getBody());
        assertEquals(statementId, response.getBody().getStatementId());

        verify(loanService).selectOffer(request);
        verifyNoMoreInteractions(loanService);
    }
}
