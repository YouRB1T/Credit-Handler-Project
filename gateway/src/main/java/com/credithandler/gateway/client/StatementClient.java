package com.credithandler.gateway.client;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "gateway-statement-client",
        url = "${gateway.services.statement-url}"
)
public interface StatementClient {

    @PostMapping("/statement")
    List<LoanOfferDto> createStatement(@RequestBody LoanStatementRequestDto request);

    @PostMapping("/statement/offer")
    StatementDto selectOffer(@RequestBody LoanOfferDto request);
}
