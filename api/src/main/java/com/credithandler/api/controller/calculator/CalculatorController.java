package com.credithandler.api.controller.calculator;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "Сервис вычисления кредитной заявки и возможных предложений",
        description = "Сервис предоставляет возможность вычисления кредитного предложения на основе данных скоринга," +
                "а также предоставление нескольких кредитных предложений на основе данных клиента")
public interface CalculatorController {

    @PostMapping("/offers")
    @Operation(
            summary = "Список кредитных предложений",
            description = "На основе данных заявки возвращает список доступных кредитных предложений, прескоринг"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получены предложения",
                    content = @Content(schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<LoanOfferDto>> provisionLoanOffers(@Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/calc")
    @Operation(
            summary = "Вычисление кредитной заявки",
            description = "На основе данных происходит из валидация, скоринг и вычисление кредитного предложения"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно создано кредитное предложение",
                    content = @Content(schema = @Schema(implementation = CreditDto.class))),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<CreditDto> calculateCredit(@Valid @RequestBody ScoringDataDto request);
}
