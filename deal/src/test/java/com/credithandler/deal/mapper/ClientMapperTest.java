package com.credithandler.deal.mapper;

import com.credithandler.api.dto.loan.LoanStatementRequestDto;
import com.credithandler.deal.model.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Java6Assertions.assertThat;
@DisplayName("Тестирование маппера клиента")

class ClientMapperTest {

    private final ClientMapper mapper = Mappers.getMapper(ClientMapper.class);

    @Test
    @DisplayName("Маппинг LoanStatementRequestDto -> Client")
    void toEntity_shouldMapCorrectly() {

        LoanStatementRequestDto dto = LoanStatementRequestDto.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .middleName("Ivanovich")
                .email("ivan@test.com")
                .birthdate(LocalDate.of(1990, 1, 1))
                .build();

        Client result = mapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Ivan");
        assertThat(result.getLastName()).isEqualTo("Ivanov");
        assertThat(result.getMiddleName()).isEqualTo("Ivanovich");
        assertThat(result.getEmail()).isEqualTo("ivan@test.com");

        assertThat(result.getClientId()).isNull();
        assertThat(result.getPassport()).isNull();
        assertThat(result.getGender()).isNull();
    }
}
