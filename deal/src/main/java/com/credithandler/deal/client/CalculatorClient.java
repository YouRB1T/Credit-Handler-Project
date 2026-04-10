package com.credithandler.deal.client;

import com.credithandler.api.controller.calculator.CalculatorController;
import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "calculator-client",
        url = "${calculator.url:http://localhost:8081}",
        path = "/calculator"
)
public interface CalculatorClient extends CalculatorController {

    @PostMapping("/offers")
    List<LoanOfferDto> getLoanOffers(@RequestBody LoanStatementRequestDto request);

    @PostMapping("/calc")
    ResponseEntity<CreditDto> calculateCredit(@RequestBody ScoringDataDto request);
}
