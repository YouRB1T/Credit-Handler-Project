package com.credithandler.deal.controller;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.deal.service.StatementAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@DisplayName("Тестирование админского контроллера deal")

class DealAdminControllerTest {

    private final StatementAdminService statementAdminService = mock(StatementAdminService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new DealAdminController(statementAdminService))
            .build();

    @Test
    @DisplayName("GET /deal/admin/statement/{statementId} возвращает заявку")
    void getStatementByIdReturnsStatement() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementDto statementDto = new StatementDto();
        statementDto.setStatementId(statementId);
        when(statementAdminService.getStatementById(statementId)).thenReturn(statementDto);

        mockMvc.perform(get("/deal/admin/statement/" + statementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(statementId.toString()));

        verify(statementAdminService).getStatementById(statementId);
    }

    @Test
    @DisplayName("GET /deal/admin/statement возвращает все заявки")
    void getAllStatementsReturnsAllStatements() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementDto statementDto = new StatementDto();
        statementDto.setStatementId(statementId);
        when(statementAdminService.getAllStatements()).thenReturn(List.of(statementDto));

        mockMvc.perform(get("/deal/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statementId").value(statementId.toString()));

        verify(statementAdminService).getAllStatements();
    }
}
