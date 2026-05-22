package com.credithandler.deal.controller;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.deal.service.DealService;
import com.credithandler.deal.service.StatementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тестирование контроллера deal")
class DealControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatementService statementService;

    @MockitoBean
    private DealService dealService;

    @Test
    @DisplayName("POST /calculateStatements - успех")
    void calculateStatements_success() throws Exception {

        LoanStatementRequestDto request = LoanStatementRequestDto.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@test.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .amount(new BigDecimal("100000"))
                .term(12)
                .build();

        LoanOfferDto offer = new LoanOfferDto();
        offer.setRequestedAmount(new BigDecimal("100000"));

        when(statementService.createStatement(any()))
                .thenReturn(List.of(offer));

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestedAmount").value(100000));

        verify(statementService).createStatement(any());
    }

    @Test
    @DisplayName("POST /deal/select - успех")
    void selectOffer_success() throws Exception {

        LoanOfferDto request = new LoanOfferDto();
        request.setStatementId(UUID.randomUUID());

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(dealService).selectOfferForDeal(any());
    }

    @Test
    @DisplayName("POST /deal/calculate/{statementId} - успех")
    void registration_success() throws Exception {

        UUID statementId = UUID.randomUUID();

        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setDependentAmount(1);
        request.setPassportIssueDate(LocalDate.now().minusYears(1));
        request.setPassportIssueBranch("123-456");
        request.setAccountNumber("12345678901234567890");

        mockMvc.perform(post("/deal/calculate/" + statementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(dealService).registrationDealAndCountCredit(any(), eq(statementId));
    }

    @Test
    @DisplayName("Ошибка если сервис кидает BusinessException")
    void shouldReturnUnprocessableEntity_whenBusinessException() throws Exception {

        when(statementService.createStatement(any()))
                .thenThrow(BusinessException.of("Ошибка", "Описание"));

        LoanStatementRequestDto request = LoanStatementRequestDto.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@test.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .amount(new BigDecimal("100000"))
                .term(12)
                .build();

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }
}
