package com.credithandler.deal.service.impl;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.mapper.ClientMapper;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Passport;
import com.credithandler.deal.repository.ClientRepository;
import com.credithandler.deal.service.ClientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public Client createClient(LoanStatementRequestDto request) {
        log.info(">> createClient, request: {}", request);

        Client client = clientMapper.toEntity(request);

        client.setBirthdate(request.getBirthdate());

        Passport passport = Passport.builder()
                .passportId(UUID.randomUUID())
                .series(request.getPassportSeries())
                .number(request.getPassportNumber())
                .issueDate(null)
                .issueBranch(null)
                .build();
        client.setPassport(passport);

        Client savedClient = clientRepository.save(client);

        log.info("<< createClient, clientId: {}", savedClient.getClientId());
        return savedClient;
    }
}
