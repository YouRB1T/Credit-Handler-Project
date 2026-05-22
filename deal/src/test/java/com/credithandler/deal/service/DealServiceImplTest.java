package com.credithandler.deal.service;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.client.CalculatorClient;
import com.credithandler.deal.constants.ErrorConstants;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.deal.mapper.CreditMapper;
import com.credithandler.deal.mapper.ScoringDataMapper;
import com.credithandler.deal.mapper.StatementMapper;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Credit;
import com.credithandler.deal.model.Statement;
import com.credithandler.deal.model.enums.ApplicationStatus;
import com.credithandler.deal.repository.ClientRepository;
import com.credithandler.deal.repository.CreditRepository;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.EmailMessageProducer;
import com.credithandler.deal.service.impl.DealServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование DealServiceImpl")
public class DealServiceImplTest {

    @Mock
    private StatementRepository statementRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CreditRepository creditRepository;

    @Mock
    private ScoringDataMapper scoringDataMapper;

    @Mock
    private CreditMapper creditMapper;

    @Mock
    private StatementMapper statementMapper;

    @Mock
    private CalculatorClient calculatorClient;

    @Mock
    private EmailMessageProducer emailMessageProducer;

    @InjectMocks
    private DealServiceImpl dealService;

    private UUID statementId;
    private Statement statement;
    private Client client;
    private LoanOfferDto offer;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();

        offer = new LoanOfferDto();
        offer.setStatementId(statementId);

        client = new Client();
        client.setClientId(UUID.randomUUID());
        client.setEmail("client@test.com");

        statement = new Statement();
        statement.setStatementId(statementId);
        statement.setClientId(client.getClientId());
        statement.setStatus(ApplicationStatus.PREAPPROVAL);
        statement.setHistoryStatus(new ArrayList<>());
    }

    @Test
    @DisplayName("Успешный выбор предложения")
    void selectOffer_success() {

        when(statementRepository.findByIdWithLock(statementId))
                .thenReturn(Optional.of(statement));
        when(clientRepository.findById(client.getClientId()))
                .thenReturn(Optional.of(client));
        when(statementMapper.toDto(statement))
                .thenReturn(new com.credithandler.api.dto.model.StatementDto());

        dealService.selectOfferForDeal(offer);

        assertThat(statement.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(statement.getAppliedOffer()).isEqualTo(offer);
        assertThat(statement.getHistoryStatus()).hasSize(1);

        verify(statementRepository).save(statement);
    }

    @Test
    @DisplayName("Ошибка если заявка не найдена")
    void selectOffer_statementNotFound() {

        when(statementRepository.findByIdWithLock(statementId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dealService.selectOfferForDeal(offer))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.STATEMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка если статус не PREAPPROVAL")
    void selectOffer_invalidStatus() {

        statement.setStatus(ApplicationStatus.APPROVED);

        when(statementRepository.findByIdWithLock(statementId))
                .thenReturn(Optional.of(statement));

        assertThatThrownBy(() -> dealService.selectOfferForDeal(offer))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.INVALID_STATEMENT_STATUS);
    }

    @Test
    @DisplayName("Успешная регистрация сделки и расчёт кредита")
    void registration_success() {

        statement.setStatus(ApplicationStatus.APPROVED);

        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setDependentAmount(1);
        request.setAccountNumber("123456");

        CreditDto creditDto = new CreditDto();
        Credit credit = new Credit();
        credit.setCreditId(UUID.randomUUID());

        when(statementRepository.findById(statementId))
                .thenReturn(Optional.of(statement));

        when(clientRepository.findById(client.getClientId()))
                .thenReturn(Optional.of(client));

        when(scoringDataMapper.toScoringDataDto(request, client, statement))
                .thenReturn(new ScoringDataDto());

        when(calculatorClient.calculateCredit(any()))
                .thenReturn(ResponseEntity.ok(creditDto));

        when(creditMapper.toEntity(creditDto))
                .thenReturn(credit);

        when(creditRepository.save(any()))
                .thenReturn(credit);
        when(statementRepository.save(statement))
                .thenReturn(statement);
        when(statementMapper.toDto(statement))
                .thenReturn(new com.credithandler.api.dto.model.StatementDto());

        dealService.registrationDealAndCountCredit(request, statementId);

        assertThat(statement.getStatus()).isEqualTo(ApplicationStatus.CC_APPROVED);
        assertThat(statement.getCreditId()).isEqualTo(credit.getCreditId());
        assertThat(statement.getHistoryStatus()).hasSize(1);

        verify(clientRepository).save(client);
        verify(creditRepository).save(credit);
        verify(statementRepository).save(statement);
    }

    @Test
    @DisplayName("Ошибка если заявка не найдена")
    void registration_statementNotFound() {

        when(statementRepository.findById(statementId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                dealService.registrationDealAndCountCredit(new FinishRegistrationRequestDto(), statementId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.STATEMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка если статус не APPROVED")
    void registration_invalidStatus() {

        statement.setStatus(ApplicationStatus.PREAPPROVAL);

        when(statementRepository.findById(statementId))
                .thenReturn(Optional.of(statement));

        assertThatThrownBy(() ->
                dealService.registrationDealAndCountCredit(new FinishRegistrationRequestDto(), statementId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.INVALID_STATEMENT_STATUS);
    }

    @Test
    @DisplayName("Ошибка если клиент не найден")
    void registration_clientNotFound() {

        statement.setStatus(ApplicationStatus.APPROVED);

        when(statementRepository.findById(statementId))
                .thenReturn(Optional.of(statement));

        when(clientRepository.findById(statement.getClientId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                dealService.registrationDealAndCountCredit(new FinishRegistrationRequestDto(), statementId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.CLIENT_NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка если кредит не рассчитан")
    void registration_creditNull() {

        statement.setStatus(ApplicationStatus.APPROVED);

        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setDependentAmount(1);
        request.setPassportIssueDate(LocalDate.now().minusYears(1));
        request.setPassportIssueBranch("123-456");
        request.setAccountNumber("12345678901234567890");

        when(statementRepository.findById(statementId))
                .thenReturn(Optional.of(statement));

        when(clientRepository.findById(client.getClientId()))
                .thenReturn(Optional.of(client));

        when(scoringDataMapper.toScoringDataDto(any(), any(), any()))
                .thenReturn(new ScoringDataDto());

        when(calculatorClient.calculateCredit(any()))
                .thenReturn(null);

        assertThatThrownBy(() ->
                dealService.registrationDealAndCountCredit(request, statementId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorConstants.CREDIT_NOT_CALCULATED);
    }
}
