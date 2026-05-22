package com.credithandler.gateway.controller.api;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
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

import java.util.UUID;

@Tag(name = "Gateway: сделка", description = "Единая точка входа для расчета кредита и работы с документами")
public interface GatewayDealControllerApi {

    @PostMapping("/statement/registration/{statementId}")
    @Operation(summary = "Завершение регистрации", description = "Проксирует расчет кредита в deal-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Кредит рассчитан",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> registrationDealAndCountCredit(
            @Valid @RequestBody FinishRegistrationRequestDto request,
            @PathVariable UUID statementId);

    @PostMapping("/document/{statementId}")
    @Operation(summary = "Отправка документов", description = "Проксирует запрос отправки документов в deal-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос отправки документов принят",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> sendDocuments(@PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/sign")
    @Operation(summary = "Запрос подписи документов", description = "Проксирует генерацию SES-кода в deal-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SES-код создан",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> signDocuments(@PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/sign/code")
    @Operation(summary = "Подписание документов SES-кодом", description = "Проксирует проверку SES-кода в deal-service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Документы подписаны",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> codeDocuments(
            @Valid @RequestBody SesCodeRequestDto request,
            @PathVariable UUID statementId);
}
