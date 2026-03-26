package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.mapper.StatementMapper;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Statement;
import com.credithandler.deal.repository.StatementRepository;
import com.credithandler.deal.service.CalculatingForClient;
import com.credithandler.deal.service.ClientService;
import com.credithandler.deal.service.StatementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {
    private final StatementRepository statementRepository;
    private final ClientService clientService;
    private final CalculatingForClient calculatorClient;
    private final StatementMapper statementMapper;

    @Override
    @Transactional
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        log.info(">> createStatement, request: {}", request);

        Client client = clientService.createClient(request);
        log.debug("Клиент создан: {}", client.getClientId());

        Statement statement = statementMapper.toEntity(request, client);
        Statement savedStatement = statementRepository.save(statement);
        log.debug("Заявка создана: {}", savedStatement.getStatementId());

        List<LoanOfferDto> offers = calculatorClient.getLoanOffers(request);
        log.debug("Получено {} предложений от калькулятора", offers.size());

        UUID statementId = savedStatement.getStatementId();
        offers.forEach(offer -> offer.setStatementId(statementId));

        savedStatement.setAppliedOffers(offers);
        statementRepository.save(savedStatement);
        log.debug("Предложения сохранены в заявку");

        log.info("<< createStatement, возвращено {} предложений", offers.size());
        return offers;
    }
}
