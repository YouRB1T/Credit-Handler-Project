package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.constants.ErrorConstants;
import com.credithandler.deal.exception.BusinessException;
import com.credithandler.deal.mapper.StatementMapper;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Statement;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.impl.StatementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование StatementServiceImpl")
class StatementServiceImplTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private CalculatingForClient calculatorClient;

    @Mock
    private StatementMapper statementMapper;

    @InjectMocks
    private StatementServiceImpl service;

    private LoanStatementRequestDto request;
    private Client client;
    private Statement statement;
    private List<LoanOfferDto> offers;

    @BeforeEach
    void setUp() {
        request = new LoanStatementRequestDto();

        client = new Client();
        client.setClientId(UUID.randomUUID());

        statement = new Statement();
        statement.setStatementId(UUID.randomUUID());

        LoanOfferDto offer = new LoanOfferDto();
        offers = List.of(offer);
    }

    @Test
    @DisplayName("Успешное создание заявки")
    void createStatement_success() {

        when(clientService.createClient(request)).thenReturn(client);
        when(statementMapper.toEntity(request, client)).thenReturn(statement);
        when(statementRepository.save(any())).thenReturn(statement);
        when(calculatorClient.getLoanOffers(request)).thenReturn(offers);

        List<LoanOfferDto> result = service.createStatement(request);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getStatementId()).isEqualTo(statement.getStatementId());

        verify(clientService).createClient(request);
        verify(statementMapper).toEntity(request, client);
        verify(calculatorClient).getLoanOffers(request);
    }

    @Test
    @DisplayName("Ошибка если клиент null")
    void createStatement_clientNull_shouldThrowException() {

        when(clientService.createClient(request)).thenReturn(null);

        assertThatThrownBy(() -> service.createStatement(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.CLIENT_NOT_CREATED);
    }

    @Test
    @DisplayName("Ошибка если statement null")
    void createStatement_statementNull_shouldThrowException() {

        when(clientService.createClient(request)).thenReturn(client);
        when(statementMapper.toEntity(request, client)).thenReturn(null);

        assertThatThrownBy(() -> service.createStatement(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.STATEMENT_NOT_CREATED);
    }

    @Test
    @DisplayName("Ошибка если нет предложений")
    void createStatement_offersEmpty_shouldThrowException() {

        when(clientService.createClient(request)).thenReturn(client);
        when(statementMapper.toEntity(request, client)).thenReturn(statement);
        when(statementRepository.save(any())).thenReturn(statement);
        when(calculatorClient.getLoanOffers(request)).thenReturn(List.of());

        assertThatThrownBy(() -> service.createStatement(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.OFFERS_NOT_FOUND);
    }

}
