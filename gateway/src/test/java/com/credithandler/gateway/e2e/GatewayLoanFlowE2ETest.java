package com.credithandler.gateway.e2e;

import com.credithandler.api.dto.dossier.SesCodeRequestDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.dto.model.ApplicationStatus;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.credithandler.api.dto.model.StatementDto;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.gateway.client.DealClient;
import com.credithandler.gateway.client.StatementClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("E2E тестирование кредитного сценария через gateway")
class GatewayLoanFlowE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StatementClient statementClient;

    @MockitoBean
    private DealClient dealClient;

    @Test
    @DisplayName("Клиент может пройти полный кредитный сценарий через gateway")
    void clientCanPassFullLoanFlowThroughGateway() throws Exception {
        UUID statementId = UUID.randomUUID();
        LoanStatementRequestDto statementRequest = validLoanStatementRequest();
        LoanOfferDto offer = offer(statementId);
        FinishRegistrationRequestDto registrationRequest = registrationRequest();
        SesCodeRequestDto sesCodeRequest = new SesCodeRequestDto("1234");

        when(statementClient.createStatement(any())).thenReturn(List.of(offer));
        when(statementClient.selectOffer(any())).thenReturn(statement(statementId, ApplicationStatus.APPROVED));
        when(dealClient.registrationDealAndCountCredit(any(), eq(statementId)))
                .thenReturn(statement(statementId, ApplicationStatus.CC_APPROVED));
        when(dealClient.sendDocuments(statementId)).thenReturn(statement(statementId, ApplicationStatus.PREPARE_DOCUMENTS));
        when(dealClient.signDocuments(statementId)).thenReturn(statement(statementId, ApplicationStatus.DOCUMENTS_CREATED));
        when(dealClient.codeDocuments(eq(statementId), any()))
                .thenReturn(statement(statementId, ApplicationStatus.CREDIT_ISSUED));
        when(dealClient.getStatementById(statementId)).thenReturn(statement(statementId, ApplicationStatus.CREDIT_ISSUED));
        when(dealClient.getAllStatements()).thenReturn(List.of(statement(statementId, ApplicationStatus.CREDIT_ISSUED)));

        String createStatementResponse = postJson("/statement", statementRequest);
        String selectOfferResponse = postJson("/statement/select", offer);
        String registrationResponse = postJson("/statement/registration/" + statementId, registrationRequest);
        String sendDocumentsResponse = postEmpty("/document/" + statementId);
        String signDocumentsResponse = postEmpty("/document/" + statementId + "/sign");
        String codeDocumentsResponse = postJson("/document/" + statementId + "/sign/code", sesCodeRequest);
        String adminByIdResponse = getBody("/admin/statement/" + statementId);
        String adminAllResponse = getBody("/admin/statement");

        assertThat(createStatementResponse)
                .contains(statementId.toString())
                .contains("120000");
        assertThat(selectOfferResponse).contains(ApplicationStatus.APPROVED.name());
        assertThat(registrationResponse).contains(ApplicationStatus.CC_APPROVED.name());
        assertThat(sendDocumentsResponse).contains(ApplicationStatus.PREPARE_DOCUMENTS.name());
        assertThat(signDocumentsResponse).contains(ApplicationStatus.DOCUMENTS_CREATED.name());
        assertThat(codeDocumentsResponse).contains(ApplicationStatus.CREDIT_ISSUED.name());
        assertThat(adminByIdResponse).contains(statementId.toString());
        assertThat(adminAllResponse).contains(statementId.toString());

        InOrder inOrder = inOrder(statementClient, dealClient);
        inOrder.verify(statementClient).createStatement(any());
        inOrder.verify(statementClient).selectOffer(any());
        inOrder.verify(dealClient).registrationDealAndCountCredit(any(), eq(statementId));
        inOrder.verify(dealClient).sendDocuments(statementId);
        inOrder.verify(dealClient).signDocuments(statementId);
        inOrder.verify(dealClient).codeDocuments(eq(statementId), any());
        inOrder.verify(dealClient).getStatementById(statementId);
        inOrder.verify(dealClient).getAllStatements();
    }

    @Test
    @DisplayName("Валидация gateway останавливает невалидный запрос до вызова нижестоящего сервиса")
    void gatewayValidationStopsInvalidStatementRequestBeforeDownstreamCall() throws Exception {
        LoanStatementRequestDto request = new LoanStatementRequestDto();

        mockMvc.perform(post("/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        org.mockito.Mockito.verifyNoInteractions(statementClient, dealClient);
    }

    private String postJson(String url, Object body) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String postEmpty(String url) throws Exception {
        MvcResult result = mockMvc.perform(post(url))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    private String getBody(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString();
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

    private FinishRegistrationRequestDto registrationRequest() {
        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setDependentAmount(0);
        request.setPassportIssueDate(LocalDate.now().minusYears(1));
        request.setPassportIssueBranch("123-456");
        request.setAccountNumber("40817810099910004312");
        return request;
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

    private StatementDto statement(UUID statementId, ApplicationStatus status) {
        StatementDto statement = new StatementDto();
        statement.setStatementId(statementId);
        statement.setEmail("ivan@test.com");
        statement.setStatus(status);
        return statement;
    }
}
