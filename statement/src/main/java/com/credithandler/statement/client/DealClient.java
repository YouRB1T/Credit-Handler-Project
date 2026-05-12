package com.credithandler.statement.client;

import com.credithandler.api.controller.DealController;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "deal-client",
        url = "${deal.url:http://localhost:8082}",
        path = "/deal"
)
public interface DealClient extends DealController {

    @PostMapping("/statement")
    ResponseEntity<List<LoanOfferDto>> calculateStatements(LoanStatementRequestDto request);

    @PostMapping("/offer/select")
    ResponseEntity<StatementDto> selectOfferForDeal(LoanOfferDto request);

    @PostMapping("/calculate/{statementId}")
    ResponseEntity<StatementDto> registrationDealAndCountCredit(FinishRegistrationRequestDto request,
                                                                @PathVariable UUID statementId);
}
