package com.credithandler.dossier.client;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.model.UpdateStatementStatusRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "dossier-deal-client",
        url = "${deal.url:http://localhost:8082}",
        path = "/deal"
)
public interface DealClient {

    @PutMapping("/admin/statement/{statementId}/status")
    ResponseEntity<StatementDto> updateStatementStatus(@PathVariable UUID statementId,
                                                       @RequestBody UpdateStatementStatusRequestDto request);
}
