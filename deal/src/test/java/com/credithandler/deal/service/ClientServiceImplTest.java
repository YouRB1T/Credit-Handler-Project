package com.credithandler.deal.service;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.mapper.ClientMapper;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.repository.ClientRepository;
import com.credithandler.deal.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ClientServiceImpl")
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private LoanStatementRequestDto request;
    private Client client;

    @BeforeEach
    void setUp() {
        request = new LoanStatementRequestDto();
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");

        client = new Client();
        client.setClientId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Успешное создание клиента")
    void createClient_shouldSaveClient() {

        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.createClient(request);

        assertThat(result).isNotNull();
        assertThat(result.getPassport()).isNotNull();
        assertThat(result.getPassport().getSeries()).isEqualTo("1234");
        assertThat(result.getPassport().getNumber()).isEqualTo("567890");

        verify(clientMapper).toEntity(request);
        verify(clientRepository).save(client);
    }
}
