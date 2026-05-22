package com.credithandler.statement;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.statement.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Интеграционное тестирование StatementController")
class StatementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    @Test
    @DisplayName("POST /statement возвращает предложения из сервиса")
    void createStatement_shouldReturnOffersFromService() throws Exception {
        UUID statementId = UUID.randomUUID();
        LoanStatementRequestDto request = validLoanStatementRequest();
        LoanOfferDto offer = offer(statementId);

        when(loanService.prescoring(any())).thenReturn(List.of(offer));

        MvcResult result = mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains(statementId.toString())
                .contains("100000")
                .contains("120000");

        verify(loanService).prescoring(any());
    }

    @Test
    @DisplayName("POST /statement отклоняет невалидный запрос до вызова сервиса")
    void createStatement_whenRequestInvalid_shouldReturnUnprocessableEntity() throws Exception {
        LoanStatementRequestDto request = new LoanStatementRequestDto();

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(loanService);
    }

    @Test
    @DisplayName("POST /statement/offer возвращает выбранную заявку")
    void selectOffer_shouldReturnSelectedStatement() throws Exception {
        UUID statementId = UUID.randomUUID();
        StatementDto statement = new StatementDto();
        statement.setStatementId(statementId);
        statement.setStatus(ApplicationStatus.APPROVED);

        when(loanService.selectOffer(any())).thenReturn(statement);

        MvcResult result = mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offer(statementId))))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .contains(statementId.toString())
                .contains(ApplicationStatus.APPROVED.name());

        verify(loanService).selectOffer(any());
    }

    private LoanStatementRequestDto validLoanStatementRequest() {
        return LoanStatementRequestDto.builder()
                .amount(new BigDecimal("100000"))
                .term(12)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivan@test.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .passportSeries("1234")
                .passportNumber("123456")
                .build();
    }

    private LoanOfferDto offer(UUID statementId) {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(statementId);
        offer.setRequestedAmount(new BigDecimal("100000"));
        offer.setTotalAmount(new BigDecimal("120000"));
        offer.setTerm(12);
        offer.setMonthlyPayment(new BigDecimal("10000"));
        offer.setRate(new BigDecimal("12.5"));
        offer.setIsInsuranceEnabled(true);
        offer.setIsSalaryClient(true);
        return offer;
    }
}
