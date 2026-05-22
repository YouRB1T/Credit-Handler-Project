package com.credithandler.api.controller;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.StatementDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Tag(name = "Администрирование заявок",
        description = "Административные API для просмотра кредитных заявок")
public interface DealAdminController {

    @GetMapping("/statement/{statementId}")
    @Operation(
            summary = "Получение заявки по id",
            description = "Возвращает полную информацию о кредитной заявке по идентификатору"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заявка найдена",
                    content = @Content(schema = @Schema(implementation = StatementDto.class))),
            @ApiResponse(responseCode = "404", description = "Заявка не найдена",
                    content = @Content(schema = @Schema(implementation = BusinessException.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<StatementDto> getStatementById(@PathVariable UUID statementId);

    @GetMapping("/statement")
    @Operation(
            summary = "Получение всех заявок",
            description = "Возвращает список всех кредитных заявок"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заявок получен",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatementDto.class)))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    ResponseEntity<List<StatementDto>> getAllStatements();
}
