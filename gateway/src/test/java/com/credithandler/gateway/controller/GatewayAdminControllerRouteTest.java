package com.credithandler.gateway.controller;

import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.gateway.service.GatewayDealService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Тестирование маршрутов админского контроллера gateway")
class GatewayAdminControllerRouteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GatewayDealService gatewayDealService;

    @Test
    @DisplayName("GET /admin/statement/{statementId} проксирует получение заявки по id")
    void getStatementByIdRouteProxiesDealAdminApi() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementDto statementDto = new StatementDto();
        statementDto.setStatementId(statementId);
        when(gatewayDealService.getStatementById(statementId)).thenReturn(statementDto);

        mockMvc.perform(get("/admin/statement/" + statementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statementId").value(statementId.toString()));

        verify(gatewayDealService).getStatementById(statementId);
    }

    @Test
    @DisplayName("GET /admin/statement проксирует получение всех заявок")
    void getAllStatementsRouteProxiesDealAdminApi() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementDto statementDto = new StatementDto();
        statementDto.setStatementId(statementId);
        when(gatewayDealService.getAllStatements()).thenReturn(List.of(statementDto));

        mockMvc.perform(get("/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statementId").value(statementId.toString()));

        verify(gatewayDealService).getAllStatements();
    }
}
