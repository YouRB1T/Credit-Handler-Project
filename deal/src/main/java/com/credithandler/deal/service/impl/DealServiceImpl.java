package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.client.CalculatorClient;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.deal.constants.ErrorConstants;
import com.credithandler.deal.mapper.CreditMapper;
import com.credithandler.deal.mapper.ScoringDataMapper;
import com.credithandler.deal.model.*;
import com.credithandler.deal.model.enums.*;
import com.credithandler.deal.repository.ClientRepository;
import com.credithandler.deal.repository.CreditRepository;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.DealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.credithandler.deal.model.enums.ApplicationStatus.PREAPPROVAL;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {

    private final StatementRepository statementRepository;
    private final ClientRepository clientRepository;
    private final CreditRepository creditRepository;

    private final ScoringDataMapper scoringDataMapper;
    private final CreditMapper creditMapper;

    private final CalculatorClient calculatorClient;

    @Override
    @Transactional
    public void selectOfferForDeal(LoanOfferDto request) {
        log.info(">> selectOfferForDeal, request: {}", request);

        UUID statementId = request.getStatementId();

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> {
                    log.error("Заявка не найдена: {}", statementId);
                    return BusinessException.of(
                            ErrorConstants.STATEMENT_NOT_FOUND,
                            String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                    );
                });

        if (statement.getStatus() != PREAPPROVAL) {
            log.warn("Заявка уже обработана, текущий статус: {}", statement.getStatus());
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, statement.getStatus())
            );
        }

        ApplicationStatus oldStatus = statement.getStatus();
        statement.setStatus(ApplicationStatus.APPROVED);
        log.debug("Статус обновлён: {} -> {}", oldStatus, ApplicationStatus.APPROVED);

        HistoryStatus history = HistoryStatus.builder()
                .status(ApplicationStatus.APPROVED.toString())
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.MANUAL)
                .build();
        statement.getHistoryStatus().add(history);
        log.debug("Добавлена запись в историю статусов");

        statement.setAppliedOffer(request);
        log.debug("Установлено выбранное предложение: ставка {}%, ежемесячный платеж {}",
                request.getRate(), request.getMonthlyPayment());

        statementRepository.save(statement);
        log.debug("Заявка сохранена");

        log.info("<< selectOfferForDeal, statementId: {}, status: {}", statementId, statement.getStatus());
    }

    @Override
    @Transactional
    public void registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
        log.info(">> registrationDealAndCountCredit, statementId: {}, request: {}", statementId, request);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.STATEMENT_NOT_FOUND,
                        String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                ));

        if (statement.getStatus() != ApplicationStatus.APPROVED) {
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, ApplicationStatus.APPROVED)
            );
        }

        Client client = clientRepository.findById(statement.getClientId())
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.CLIENT_NOT_FOUND,
                        String.format(ErrorConstants.CLIENT_NOT_FOUND_DESC, statement.getClientId())
                ));

        client.setGender(Gender.valueOf(request.getGender().toString()));
        client.setMaritalStatus(MaritalStatus.valueOf(request.getMaritalStatus().toString()));
        client.setDependentAmount(request.getDependentAmount());
        client.setAccountNumber(request.getAccountNumber());

        if (client.getPassport() != null) {
            Passport passport = client.getPassport();
            passport.setIssueDate(request.getPassportIssueDate());
            passport.setIssueBranch(request.getPassportIssueBranch());
        } else {
            Passport passport = Passport.builder()
                    .passportId(UUID.randomUUID())
                    .issueDate(request.getPassportIssueDate())
                    .issueBranch(request.getPassportIssueBranch())
                    .build();
            client.setPassport(passport);
        }

        clientRepository.save(client);
        log.debug("Клиент обновлён: {}", client.getClientId());

        ScoringDataDto scoringData = scoringDataMapper.toScoringDataDto(request, client, statement);
        log.debug("ScoringDataDto сформирован: {}", scoringData);

        CreditDto creditDto = calculatorClient.calculateCredit(scoringData);
        if (creditDto == null) {
            throw BusinessException.of(
                    ErrorConstants.CREDIT_NOT_CALCULATED,
                    ErrorConstants.CREDIT_NOT_CALCULATED_DESC
            );
        }
        log.debug("CreditDto получен от калькулятора");

        Credit credit = creditMapper.toEntity(creditDto);
        credit.setCreditStatus(CreditStatus.CALCULATED);
        Credit savedCredit = creditRepository.save(credit);

        statement.setCreditId(savedCredit.getCreditId());
        statement.setStatus(ApplicationStatus.CC_APPROVED);

        HistoryStatus history = HistoryStatus.builder()
                .status(ApplicationStatus.CC_APPROVED.toString())
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        statement.getHistoryStatus().add(history);

        statementRepository.save(statement);

        log.info("<< registrationDealAndCountCredit, creditId: {}", savedCredit.getCreditId());
    }
}
