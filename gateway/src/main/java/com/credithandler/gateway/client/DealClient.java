package com.credithandler.gateway.client;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "gateway-deal-client",
        url = "${gateway.services.deal-url}"
)
public interface DealClient {

    @PostMapping("/deal/calculate/{statementId}")
    StatementDto registrationDealAndCountCredit(
            @RequestBody FinishRegistrationRequestDto request,
            @PathVariable("statementId") UUID statementId);

    @PostMapping("/deal/document/{statementId}/send")
    StatementDto sendDocuments(@PathVariable("statementId") UUID statementId);

    @PostMapping("/deal/document/{statementId}/sign")
    StatementDto signDocuments(@PathVariable("statementId") UUID statementId);

    @PostMapping("/deal/document/{statementId}/code")
    StatementDto codeDocuments(
            @PathVariable("statementId") UUID statementId,
            @RequestBody SesCodeRequestDto request);

    @GetMapping("/deal/admin/statement/{statementId}")
    StatementDto getStatementById(@PathVariable("statementId") UUID statementId);

    @GetMapping("/deal/admin/statement")
    List<StatementDto> getAllStatements();
}
