package com.credithandler.statement;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.statement.client.DealClient;
import com.credithandler.statement.service.impl.LoanServiceImpl;
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
class LoanServiceImplTest {

    @Mock
    private DealClient dealClient;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    void prescoringShouldReturnLoanOffers() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();

        LoanOfferDto offer1 = new LoanOfferDto();
        LoanOfferDto offer2 = new LoanOfferDto();
        List<LoanOfferDto> expectedOffers = List.of(offer1, offer2);

        when(dealClient.calculateStatements(request))
                .thenReturn(ResponseEntity.ok(expectedOffers));

        List<LoanOfferDto> actualOffers = loanService.prescoring(request);

        assertEquals(expectedOffers, actualOffers);
        verify(dealClient).calculateStatements(request);
        verifyNoMoreInteractions(dealClient);
    }

    @Test
    void prescoringShouldReturnNullWhenClientBodyIsNull() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();

        when(dealClient.calculateStatements(request))
                .thenReturn(ResponseEntity.ok(null));

        List<LoanOfferDto> actualOffers = loanService.prescoring(request);

        assertNull(actualOffers);
        verify(dealClient).calculateStatements(request);
    }

    @Test
    void selectOfferShouldReturnStatementDto() {
        LoanOfferDto offer = new LoanOfferDto();

        StatementDto expectedStatement = new StatementDto();
        UUID statementId = UUID.randomUUID();
        expectedStatement.setStatementId(statementId);

        when(dealClient.selectOfferForDeal(offer))
                .thenReturn(ResponseEntity.ok(expectedStatement));

        StatementDto actualStatement = loanService.selectOffer(offer);

        assertEquals(expectedStatement, actualStatement);
        assertEquals(statementId, actualStatement.getStatementId());

        verify(dealClient).selectOfferForDeal(offer);
        verifyNoMoreInteractions(dealClient);
    }

    @Test
    void selectOfferShouldReturnNullWhenClientBodyIsNull() {
        LoanOfferDto offer = new LoanOfferDto();

        when(dealClient.selectOfferForDeal(offer))
                .thenReturn(ResponseEntity.ok(null));

        StatementDto actualStatement = loanService.selectOffer(offer);

        assertNull(actualStatement);
        verify(dealClient).selectOfferForDeal(offer);
    }
}