package com.credithandler.calculator.controller;

import com.credithandler.api.controller.calculator.dto.calc.ScoringDataDto;
import com.credithandler.api.controller.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.calculator.utils.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Интеграционное тестирование CalculatorController")
class CalculatorControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    // Тесты на валидные и невалидные входные и выходные данные
    @Test
    void provisionLoanOffers_whenValidRequest_thenReturnsListOfOffers() throws Exception {

        LoanStatementRequestDto request = TestDataFactory.createValidLoanStatementRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson)
                .isNotEmpty()
                .contains("statementId")
                .contains("totalAmount");
    }

    @Test
    void provisionLoanOffers_whenInvalidData_thenReturnsBadRequest() throws Exception {

        LoanStatementRequestDto request = TestDataFactory.createEmptyLoanStatementRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // Тесты на бизнес ошибки немножечко
    @Test
    void calculateCredit_whenDataFailsScoring_thenReturnsUnprocessableEntity() throws Exception {

        ScoringDataDto request = TestDataFactory.createScoringDataWithUnemployed();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);

        assertThat(responseMap)
                .containsEntry("error", "Бизнес ошибка")
                .containsEntry("status", 422)
                .containsKey("timestamp");

        assertThat((String) responseMap.get("message"))
                .contains("Статус занятости");
    }

    @Test
    void calculateCredit_whenLoanTooHigh_thenReturnsUnprocessableEntity() throws Exception {
        ScoringDataDto request = TestDataFactory.createScoringDataWithHighAmount();
        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is(422))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody)
                .contains("Бизнес ошибка")
                .contains("Сумма кредита");
    }

    @Test
    void calculateCredit_whenAgeTooLow_thenReturnsBadRequest() throws Exception {

        ScoringDataDto request = TestDataFactory.createScoringDataWithInvalidAge(17);
        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        assertThat(responseBody)
                .contains("Ошибка валидации")
                .contains("birthdate")
                .contains("совершеннолетним");
    }
}