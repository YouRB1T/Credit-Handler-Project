package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.client.CalculatorClient;
import com.credithandler.deal.exception.BusinessException;
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
                            "Заявка",
                            "Заявка с ID " + statementId + " не найдена"
                    );
                });

        if (statement.getStatus() != PREAPPROVAL) {
            log.warn("Заявка уже обработана, текущий статус: {}", statement.getStatus());
            throw new IllegalStateException("Заявка уже находится в статусе " + statement.getStatus());
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

        // 1. Достаём заявку
        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new RuntimeException("Заявка с ID " + statementId + " не найдена"));

        // 2. Проверяем статус
        if (statement.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Заявка должна быть в статусе APPROVED");
        }

        // 3. Достаём клиента
        Client client = clientRepository.findById(statement.getClientId())
                .orElseThrow(() -> new RuntimeException("Клиент с ID " + statement.getClientId() + " не найден"));

        // 4. Обновляем клиента данными из FinishRegistrationRequestDto
        client.setGender(Gender.valueOf(request.getGender().toString()));
        client.setMaritalStatus(MaritalStatus.valueOf(request.getMaritalStatus().toString()));
        client.setDependentAmount(request.getDependentAmount());
        client.setAccountNumber(request.getAccountNumber());

        // 5. Дозаполняем паспорт (добавляем дату выдачи и кем выдан)
        if (client.getPassport() != null) {
            Passport passport = client.getPassport();
            passport.setIssueDate(request.getPassportIssueDate());
            passport.setIssueBranch(request.getPassportIssueBranch());
        } else {
            // На случай, если паспорт не был создан (защита)
            Passport passport = Passport.builder()
                    .passportId(UUID.randomUUID())
                    .issueDate(request.getPassportIssueDate())
                    .issueBranch(request.getPassportIssueBranch())
                    .build();
            client.setPassport(passport);
        }

        // 6. Сохраняем обновлённого клиента
        clientRepository.save(client);
        log.debug("Клиент обновлён: {}", client.getClientId());

        // 7. Формируем ScoringDataDto (теперь все данные заполнены)
        ScoringDataDto scoringData = scoringDataMapper.toScoringDataDto(request, client, statement);
        log.debug("ScoringDataDto сформирован: {}", scoringData);

        // 8. Отправляем запрос в калькулятор
        CreditDto creditDto = calculatorClient.calculateCredit(scoringData);
        log.debug("CreditDto получен от калькулятора");

        // 9. Создаём и сохраняем Credit
        Credit credit = creditMapper.toEntity(creditDto);
        Credit savedCredit = creditRepository.save(credit);

        // 10. Обновляем заявку
        statement.setCreditId(savedCredit.getCreditId());
        statement.setStatus(ApplicationStatus.CC_APPROVED);

        // 11. Добавляем историю статусов
        HistoryStatus history = HistoryStatus.builder()
                .status(ApplicationStatus.CC_APPROVED.toString())
                .timestamp(LocalDateTime.now())
                .changeType(ChangeType.AUTOMATIC)
                .build();
        statement.getHistoryStatus().add(history);

        // 12. Сохраняем заявку
        statementRepository.save(statement);

        log.info("<< registrationDealAndCountCredit, creditId: {}", savedCredit.getCreditId());
    }
}
