package com.credithandler.api.controller;

import com.credithandler.api.dto.error.BusinessException;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "Сервис управления заявками (Statement)",
        description = "Обработка прескоринга и выбор конкретных кредитных предложений")
public interface StatementController {

    @PostMapping()
    @Operation(
            summary = "Прескоринг и расчет кредитных предложений",
            description = "Принимает первичные данные клиента, создает заявку и возвращает список доступных предложений."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список предложений успешно сформирован",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class)))),
            @ApiResponse(responseCode = "422", description = "Ошибка бизнес-логики (прескоринг не пройден)",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<LoanOfferDto>> createStatement(
            @Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/offer")
    @Operation(
            summary = "Выбор кредитного предложения",
            description = "Сохраняет выбранное клиентом предложение в базу данных."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предложение успешно зафиксировано"),
            @ApiResponse(responseCode = "422", description = "Ошибка выбора предложения (некорректные данные)",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> selectOffer(
            @Valid @RequestBody LoanOfferDto request);
}
