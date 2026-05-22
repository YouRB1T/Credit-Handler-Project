package com.credithandler.deal.mapper;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("Тестирование маппера заявки")

class StatementMapperTest {

    private final StatementMapper mapper = Mappers.getMapper(StatementMapper.class);

    @Test
    @DisplayName("Маппинг Statement")
    void toEntity_shouldMapCorrectly() {

        LoanStatementRequestDto dto = new LoanStatementRequestDto();
        Client client = new Client();
        UUID clientId = UUID.randomUUID();
        client.setClientId(clientId);

        Statement result = mapper.toEntity(dto, client);

        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);

        assertThat(result.getStatementId()).isNull();
        assertThat(result.getAppliedOffer()).isNull();
    }
}
