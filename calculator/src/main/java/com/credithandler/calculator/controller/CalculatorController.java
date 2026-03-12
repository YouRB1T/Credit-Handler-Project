package com.credithandler.calculator.controller;

import com.credithandler.calculator.dto.calc.CreditDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.dto.loan.LoanOfferDto;
import com.credithandler.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.service.CalculatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/calculator")
@RequiredArgsConstructor
@Tag(name = "Сервис вычисления кредитной заявки и возможных предложений",
        description = "Сервис предоставляет возможность вычисления кредитного предложения на основе данных скоринга," +
                "а также предоставление нескольких кредитных предложений на основе данных клиента")
public class CalculatorController {

    private final CalculatorService service;

    // TODO: можно ли указать в ApiResponse BusinessException в качестве аргумента
    @Operation(
            summary = "Список кредитных предложений",
            description = "На основе данных заявки возвращает список доступных кредитных предложений, прескоринг"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получены предложения",
                    content = @Content(schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/offers")
    @Validated
    public ResponseEntity<List<LoanOfferDto>> provisionLoanOffers(@RequestBody LoanStatementRequestDto request) {
        return ResponseEntity.ok(
                service.calculatingOffers(request)
        );
    }

    @Operation(
            summary = "Вычисление кредитной заявки",
            description = "На основе данных происходит из валидация, скоринг и вычисление кредитного предложения"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно создано кредитное предложение",
                    content = @Content(schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/calc")
    @Validated
    public ResponseEntity<CreditDto> calculateCredit(@RequestBody ScoringDataDto request) {
        return ResponseEntity.ok(
                service.calculateCredit(request)
        );
    }

}
