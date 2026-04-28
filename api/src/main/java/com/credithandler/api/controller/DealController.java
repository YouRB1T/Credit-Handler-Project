package com.credithandler.api.controller;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.StatementDto;
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
    ResponseEntity<StatementDto> selectOfferForDeal(
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
    ResponseEntity<StatementDto> registrationDealAndCountCredit(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/send")
    @Operation(
            summary = "Запрос на отправку кредитных документов",
            description = "Проверяет заявку, переводит ее на этап подготовки документов и публикует StatementDto в Kafka topic send-documents"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос на отправку документов успешно принят",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "404", description = "Заявка с указанным statementId не найдена",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "422", description = "Заявка находится в статусе, который не позволяет отправить документы",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> sendDocuments(@PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/sign")
    @Operation(
            summary = "Запрос на подписание кредитных документов",
            description = "Генерирует SES-код для подписания документов и переводит заявку на этап подписания"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос на подписание документов успешно принят",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "404", description = "Заявка с указанным statementId не найдена",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "422", description = "Заявка находится в статусе, который не позволяет запросить подписание",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> signDocuments(@PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/code")
    @Operation(
            summary = "Подписание документов кодом подтверждения",
            description = "Сравнивает переданный SES-код с сохраненным кодом заявки и завершает подписание документов"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Документы успешно подписаны",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "404", description = "Заявка с указанным statementId не найдена",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "422", description = "Некорректный код или заявка не готова к подписанию",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> codeDocuments(
            @Valid @RequestBody SesCodeRequestDto request,
            @PathVariable UUID statementId);
}
