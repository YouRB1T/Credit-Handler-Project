package com.credithandler.gateway.service.impl;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.client.DealClient;
import com.credithandler.gateway.service.GatewayDealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GatewayDealServiceImpl implements GatewayDealService {

    private final DealClient dealClient;

    @Override
    public StatementDto registrationDealAndCountCredit(FinishRegistrationRequestDto request, UUID statementId) {
        return dealClient.registrationDealAndCountCredit(request, statementId);
    }

    @Override
    public StatementDto sendDocuments(UUID statementId) {
        return dealClient.sendDocuments(statementId);
    }

    @Override
    public StatementDto signDocuments(UUID statementId) {
        return dealClient.signDocuments(statementId);
    }

    @Override
    public StatementDto codeDocuments(UUID statementId, SesCodeRequestDto request) {
        return dealClient.codeDocuments(statementId, request);
    }
}
