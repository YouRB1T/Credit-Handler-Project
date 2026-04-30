package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.dossier.EmailMessage;
import com.credithandler.api.dto.dossier.EmailTheme;
import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.model.UpdateStatementStatusRequestDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.client.CalculatorClient;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.deal.constants.ErrorConstants;
import com.credithandler.deal.mapper.CreditMapper;
import com.credithandler.deal.mapper.ScoringDataMapper;
import com.credithandler.deal.mapper.StatementMapper;
import com.credithandler.deal.model.*;
import com.credithandler.deal.model.enums.*;
import com.credithandler.deal.repository.ClientRepository;
import com.credithandler.deal.repository.CreditRepository;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.DealService;
import com.credithandler.deal.service.EmailMessageProducer;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
    private final StatementMapper statementMapper;
    private final EmailMessageProducer emailMessageProducer;

    @Override
    @Transactional
    public StatementDto selectOfferForDeal(LoanOfferDto request) {
        log.info(">> selectOfferForDeal, request: {}", request);

        UUID statementId = request.getStatementId();

        Statement statement = statementRepository.findByIdWithLock(statementId)
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
                .status(ApplicationStatus.APPROVED)
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

        Client client = clientRepository.findById(statement.getClientId())
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.CLIENT_NOT_FOUND,
                        String.format(ErrorConstants.CLIENT_NOT_FOUND_DESC, statement.getClientId())
                ));

        EmailMessage emailMessage = new EmailMessage(
                client.getEmail(),
                EmailTheme.FINISH_REGISTRATION,
                statement.getStatementId(),
                "Для продолжения оформления кредита завершите регистрацию."
        );
        emailMessageProducer.sendFinishRegistrationMessage(emailMessage);

        log.info("<< selectOfferForDeal, statementId: {}, status: {}", statementId, statement.getStatus());
        return statementMapper.toDto(statement);
    }

    @Override
    @Transactional
    public StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
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

        CreditDto creditDto;

        try {
            creditDto = calculatorClient.calculateCredit(scoringData).getBody();

            log.debug("CreditDto получен от калькулятора");

        } catch (FeignException ex) {

            log.error("Ошибка при вызове калькулятора: {}", ex.getMessage());

            throw BusinessException.of(
                    ErrorConstants.CREDIT_NOT_CALCULATED,
                    ex.getMessage()
            );
        }
        log.debug("CreditDto получен от калькулятора");

        Credit credit = creditMapper.toEntity(creditDto);
        credit.setCreditStatus(CreditStatus.CALCULATED);
        Credit savedCredit = creditRepository.save(credit);
        log.debug("Создан Credit: {}", savedCredit);

        statement.setCreditId(savedCredit.getCreditId());
        statement.setStatus(ApplicationStatus.CC_APPROVED);

        HistoryStatus history = HistoryStatus.builder()
                .status(ApplicationStatus.CC_APPROVED)
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();

        log.debug("Добавлена запись в историю: {}", history.toString());
        statement.getHistoryStatus().add(history);

        Statement savedStatement = statementRepository.save(statement);

        StatementDto statementDto = statementMapper.toDto(savedStatement);
        statementDto.setEmail(client.getEmail());
        emailMessageProducer.sendCreateDocumentsMessage(statementDto);

        log.info("<< registrationDealAndCountCredit, creditId: {}", savedCredit.getCreditId());

        return statementDto;
    }

    @Override
    @Transactional
    public StatementDto sendDocuments(UUID statementId) {
        log.info(">> sendDocuments, statementId: {}", statementId);

        Statement statement = statementRepository.findByIdWithLock(statementId)
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.STATEMENT_NOT_FOUND,
                        String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                ));

        if (statement.getStatus() != ApplicationStatus.CC_APPROVED
                && statement.getStatus() != ApplicationStatus.DOCUMENTS_CREATED) {
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, statement.getStatus())
            );
        }

        Client client = clientRepository.findById(statement.getClientId())
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.CLIENT_NOT_FOUND,
                        String.format(ErrorConstants.CLIENT_NOT_FOUND_DESC, statement.getClientId())
                ));

        statement.setStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        statement.getHistoryStatus().add(HistoryStatus.builder()
                .status(ApplicationStatus.PREPARE_DOCUMENTS)
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.MANUAL)
                .build());

        Statement savedStatement = statementRepository.save(statement);
        StatementDto statementDto = statementMapper.toDto(savedStatement);
        statementDto.setEmail(client.getEmail());

        emailMessageProducer.sendDocumentsMessage(statementDto);

        log.info("<< sendDocuments, statementId: {}, status: {}", statementId, statementDto.getStatus());
        return statementDto;
    }

    @Override
    @Transactional
    public StatementDto signDocuments(UUID statementId) {
        log.info(">> signDocuments, statementId: {}", statementId);

        Statement statement = statementRepository.findByIdWithLock(statementId)
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.STATEMENT_NOT_FOUND,
                        String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                ));

        if (statement.getStatus() != ApplicationStatus.DOCUMENTS_CREATED) {
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, statement.getStatus())
            );
        }

        statement.setSesCode(generateSesCode());

        Statement savedStatement = statementRepository.save(statement);
        StatementDto statementDto = statementMapper.toDto(savedStatement);

        Client client = clientRepository.findById(statement.getClientId())
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.CLIENT_NOT_FOUND,
                        String.format(ErrorConstants.CLIENT_NOT_FOUND_DESC, statement.getClientId())
                ));

        EmailMessage emailMessage = new EmailMessage(
                client.getEmail(),
                EmailTheme.SEND_SES,
                savedStatement.getStatementId(),
                "Код подтверждения подписания документов: %s".formatted(savedStatement.getSesCode())
        );
        emailMessageProducer.sendSesMessage(emailMessage);

        log.info("<< signDocuments, statementId: {}, status: {}", statementId, statementDto.getStatus());
        return statementDto;
    }

    @Override
    @Transactional
    public StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request) {
        log.info(">> codeDocuments, statementId: {}", statementId);

        Statement statement = statementRepository.findByIdWithLock(statementId)
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.STATEMENT_NOT_FOUND,
                        String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                ));

        if (statement.getStatus() != ApplicationStatus.DOCUMENTS_CREATED) {
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, statement.getStatus())
            );
        }

        if (statement.getSesCode() == null || !statement.getSesCode().equals(request.getSesCode())) {
            throw BusinessException.of(
                    "Некорректный SES-код",
                    "Переданный код подтверждения не совпадает с сохраненным кодом заявки"
            );
        }

        statement.setSignDate(LocalDateTime.now());
        statement.setStatus(ApplicationStatus.DOCUMENTS_SIGNED);
        statement.getHistoryStatus().add(HistoryStatus.builder()
                .status(ApplicationStatus.DOCUMENTS_SIGNED)
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.MANUAL)
                .build());

        statement.setStatus(ApplicationStatus.CREDIT_ISSUED);
        statement.getHistoryStatus().add(HistoryStatus.builder()
                .status(ApplicationStatus.CREDIT_ISSUED)
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build());

        Statement savedStatement = statementRepository.save(statement);
        StatementDto statementDto = statementMapper.toDto(savedStatement);

        Client client = clientRepository.findById(statement.getClientId())
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.CLIENT_NOT_FOUND,
                        String.format(ErrorConstants.CLIENT_NOT_FOUND_DESC, statement.getClientId())
                ));

        EmailMessage emailMessage = new EmailMessage(
                client.getEmail(),
                EmailTheme.CREDIT_ISSUED,
                savedStatement.getStatementId(),
                "Кредитное предложение успешно сформировано и выдано."
        );
        emailMessageProducer.sendCreditIssuedMessage(emailMessage);

        log.info("<< codeDocuments, statementId: {}, status: {}", statementId, statementDto.getStatus());
        return statementDto;
    }

    @Override
    @Transactional
    public StatementDto updateStatementStatus(UUID statementId, UpdateStatementStatusRequestDto request) {
        log.info(">> updateStatementStatus, statementId: {}, request: {}", statementId, request);

        Statement statement = statementRepository.findByIdWithLock(statementId)
                .orElseThrow(() -> BusinessException.of(
                        ErrorConstants.STATEMENT_NOT_FOUND,
                        String.format(ErrorConstants.STATEMENT_NOT_FOUND_DESC, statementId)
                ));

        if (request == null || request.getStatus() == null) {
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, null)
            );
        }

        ApplicationStatus status = ApplicationStatus.valueOf(request.getStatus().name());
        if (status != ApplicationStatus.DOCUMENTS_CREATED
                || (statement.getStatus() != ApplicationStatus.PREPARE_DOCUMENTS
                && statement.getStatus() != ApplicationStatus.DOCUMENTS_CREATED)) {
            throw BusinessException.of(
                    ErrorConstants.INVALID_STATEMENT_STATUS,
                    String.format(ErrorConstants.INVALID_STATEMENT_STATUS_DESC, statement.getStatus())
            );
        }

        if (statement.getStatus() == status) {
            StatementDto statementDto = statementMapper.toDto(statement);
            log.info("<< updateStatementStatus, statementId: {}, status: {}", statementId, statementDto.getStatus());
            return statementDto;
        }

        statement.setStatus(status);
        statement.getHistoryStatus().add(HistoryStatus.builder()
                .status(status)
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build());

        Statement savedStatement = statementRepository.save(statement);
        StatementDto statementDto = statementMapper.toDto(savedStatement);

        log.info("<< updateStatementStatus, statementId: {}, status: {}", statementId, statementDto.getStatus());
        return statementDto;
    }

    private String generateSesCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
    }
}
