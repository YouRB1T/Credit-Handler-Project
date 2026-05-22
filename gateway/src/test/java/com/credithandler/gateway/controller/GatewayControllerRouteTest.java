package com.credithandler.gateway.controller;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.service.GatewayDealService;
import com.credithandler.gateway.service.GatewayStatementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Тестирование маршрутов контроллера gateway")
class GatewayControllerRouteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GatewayStatementService gatewayStatementService;

    @MockitoBean
    private GatewayDealService gatewayDealService;

    @Test
    @DisplayName("POST /statement проксирует запрос создания заявки")
    void createStatementRouteMatchesDiagram() throws Exception {
        LoanStatementRequestDto request = LoanStatementRequestDto.builder()
                .amount(new BigDecimal("100000"))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@test.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .build();
        when(gatewayStatementService.createStatement(any())).thenReturn(List.of(new LoanOfferDto()));

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gatewayStatementService).createStatement(any());
    }

    @Test
    @DisplayName("POST /statement/select проксирует запрос выбора предложения")
    void selectOfferRouteMatchesDiagram() throws Exception {
        LoanOfferDto request = new LoanOfferDto();
        when(gatewayStatementService.selectOffer(any())).thenReturn(new StatementDto());

        mockMvc.perform(post("/statement/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gatewayStatementService).selectOffer(any());
    }

    @Test
    @DisplayName("POST /statement/registration/{statementId} проксирует запрос завершения регистрации")
    void registrationRouteMatchesDiagram() throws Exception {
        UUID statementId = UUID.randomUUID();
        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setDependentAmount(0);
        request.setPassportIssueDate(LocalDate.now().minusYears(1));
        request.setPassportIssueBranch("123-456");
        request.setAccountNumber("40817810099910004312");
        when(gatewayDealService.registrationDealAndCountCredit(any(), eq(statementId))).thenReturn(new StatementDto());

        mockMvc.perform(post("/statement/registration/" + statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gatewayDealService).registrationDealAndCountCredit(any(), eq(statementId));
    }

    @Test
    @DisplayName("POST /document/{statementId} проксирует запрос отправки документов")
    void sendDocumentsRouteMatchesDiagram() throws Exception {
        UUID statementId = UUID.randomUUID();
        when(gatewayDealService.sendDocuments(statementId)).thenReturn(new StatementDto());

        mockMvc.perform(post("/document/" + statementId))
                .andExpect(status().isOk());

        verify(gatewayDealService).sendDocuments(statementId);
    }

    @Test
    @DisplayName("POST /document/{statementId}/sign проксирует запрос подписания документов")
    void signDocumentsRouteMatchesDiagram() throws Exception {
        UUID statementId = UUID.randomUUID();
        when(gatewayDealService.signDocuments(statementId)).thenReturn(new StatementDto());

        mockMvc.perform(post("/document/" + statementId + "/sign"))
                .andExpect(status().isOk());

        verify(gatewayDealService).signDocuments(statementId);
    }

    @Test
    @DisplayName("POST /document/{statementId}/sign/code проксирует запрос проверки SES-кода")
    void codeDocumentsRouteMatchesDiagram() throws Exception {
        UUID statementId = UUID.randomUUID();
        SesCodeRequestDto request = new SesCodeRequestDto();
        request.setSesCode("1234");
        when(gatewayDealService.codeDocuments(eq(statementId), any())).thenReturn(new StatementDto());

        mockMvc.perform(post("/document/" + statementId + "/sign/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gatewayDealService).codeDocuments(eq(statementId), any());
    }
}
