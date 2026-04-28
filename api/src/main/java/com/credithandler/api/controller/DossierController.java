package com.credithandler.api.controller;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.StatementDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(
        name = "Dossier: документы и уведомления клиентов",
        description = """
                Сервис инициирует этапы документооборота кредитной сделки.
                После успешной обработки МС-deal публикует сообщение EmailMessage в Kafka,
                а МС-dossier формирует письмо, при необходимости прикладывает документы
                и отправляет уведомление клиенту на электронную почту.
                """
)
public interface DossierController {

    @PostMapping("/document/{statementId}/send")
    @Operation(
            summary = "Запрос на отправку кредитных документов",
            description = """
                    Запускает формирование пакета кредитных документов по заявке.
                    МС-deal проверяет существование заявки, переводит ее на следующий этап
                    документооборота и публикует EmailMessage в Kafka topic send-documents.
                    МС-dossier получает сообщение, формирует письмо с документами и отправляет его клиенту.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрос на отправку документов успешно принят"),
            @ApiResponse(responseCode = "404", description = "Заявка с указанным statementId не найдена",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "422", description = "Заявка находится в статусе, который не позволяет отправить документы",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<Void> send(
            @Parameter(description = "Идентификатор кредитной заявки", required = true,
                    example = "9b8a53f6-8852-4bb6-9e61-7d1f51d0c4f4")
            @PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/sign")
    @Operation(
            summary = "Запрос на подписание кредитных документов",
            description = """
                    Запускает отправку клиенту письма с кодом подтверждения для подписания документов.
                    МС-deal проверяет заявку, генерирует или сохраняет SES-код и публикует EmailMessage
                    в Kafka topic send-ses. МС-dossier отправляет клиенту письмо с кодом.
                    """
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
    ResponseEntity<StatementDto> sign(
            @Parameter(description = "Идентификатор кредитной заявки", required = true,
                    example = "9b8a53f6-8852-4bb6-9e61-7d1f51d0c4f4")
            @PathVariable UUID statementId);

    @PostMapping("/document/{statementId}/code")
    @Operation(
            summary = "Подписание документов кодом подтверждения",
            description = """
                    Подтверждает подписание документов по заявке.
                    МС-deal сверяет код подтверждения, завершает оформление кредита
                    и публикует EmailMessage в Kafka topic credit-issued.
                    МС-dossier отправляет клиенту письмо об успешной выдаче кредита.
                    Код из тела запроса сравнивается с SES-кодом, который был сгенерирован
                    на шаге /document/{statementId}/sign и сохранен в заявке.
                    """
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
    ResponseEntity<StatementDto> code(
            @Valid @RequestBody SesCodeRequestDto request,
            @Parameter(description = "Идентификатор кредитной заявки", required = true,
                    example = "9b8a53f6-8852-4bb6-9e61-7d1f51d0c4f4")
            @PathVariable UUID statementId);
}
