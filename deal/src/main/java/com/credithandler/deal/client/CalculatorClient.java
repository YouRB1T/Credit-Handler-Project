package com.credithandler.deal.client;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "calculator-client",
        url = "${calculator.url:http://localhost:8081}",
        path = "/calculator"
)
public interface CalculatorClient {

    @PostMapping("/offers")
    List<LoanOfferDto> getLoanOffers(@RequestBody LoanStatementRequestDto request);
}
