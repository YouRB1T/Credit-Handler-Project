package com.credithandler.api.controller.deal;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@RequestMapping("/deal")
@Tag(name = "Сервис вычисления кредитной сделки и расчет возможных предложений",
        description = "Сервис предоставляет возможность вычисления кредитной сделки на основе данных клиента.")
public interface DealController {

    @PostMapping("/statement")
    @Operation(
            summary = "Создание заявки и расчет кредитных предложений",
            description = "На основе данных клиента создает заявку и возвращает список доступных кредитных предложений (прескоринг)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно созданы предложения",
                    content = @Content(schema = @Schema(implementation = LoanOfferDto.class))),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<LoanOfferDto>> calculateStatements(
            @Valid @RequestBody LoanStatementRequestDto request);

    @PostMapping("/offer/select")
    @Operation(
            summary = "Выбор кредитного предложения",
            description = "Клиент выбирает одно из предложенных кредитных предложений для дальнейшего оформления"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Предложение успешно выбрано"),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<Void> selectOfferForDeal(
            @Valid @RequestBody LoanOfferDto request);

    @PostMapping("/calculate/{statementId}")
    @Operation(
            summary = "Завершение регистрации и расчет кредита",
            description = "Завершает регистрацию клиента, проводит скоринг и рассчитывает параметры кредита"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Регистрация завершена, кредит рассчитан"),
            @ApiResponse(responseCode = "422", description = "Бизнес ошибка",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<Void> registrationDealAndCountCredit(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @PathVariable UUID statementId);
}
