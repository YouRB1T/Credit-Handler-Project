package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.deal.exception.BusinessException;
import com.credithandler.deal.model.HistoryStatus;
import com.credithandler.deal.model.Statement;
import com.credithandler.deal.model.enums.ApplicationStatus;
import com.credithandler.deal.model.enums.ChangeType;
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
}
