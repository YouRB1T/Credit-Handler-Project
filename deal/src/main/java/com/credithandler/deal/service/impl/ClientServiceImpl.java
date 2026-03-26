package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.mapper.ClientMapper;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.repository.ClientRepository;
import com.credithandler.deal.service.ClientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Transactional
    public Client createClient(LoanStatementRequestDto request) {
        log.info(">> createClient, request: {}", request);

        Client client = clientMapper.toEntity(request);
        Client savedClient = clientRepository.save(client);

        log.info("<< createClient, clientId: {}", savedClient.getClientId());
        return savedClient;
    }
}
