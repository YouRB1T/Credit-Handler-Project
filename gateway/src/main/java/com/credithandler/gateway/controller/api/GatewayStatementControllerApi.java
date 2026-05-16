package com.credithandler.gateway.controller.api;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Gateway: заявки", description = "Единая точка входа для прескоринга и выбора предложения")
public interface GatewayStatementControllerApi {

    @PostMapping
    @Operation(summary = "Создание заявки", description = "Проксирует запрос создания заявки в statement-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предложения получены",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class)))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<LoanOfferDto>> createStatement(@Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/select")
    @Operation(summary = "Выбор предложения", description = "Проксирует выбор кредитного предложения в statement-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предложение выбрано",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> selectOffer(@Valid @RequestBody LoanOfferDto request);
}
