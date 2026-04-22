package com.credithandler.deal.mapper;

import com.credithandler.api.dto.calc.CreditDto;
import com.credithandler.deal.model.Credit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CreditMapperTest {

    private final CreditMapper mapper = Mappers.getMapper(CreditMapper.class);

    @Test
    @DisplayName("Маппинг CreditDto -> Credit")
    void toEntity_shouldMapCorrectly() {

        CreditDto dto = new CreditDto();
        dto.setAmount(new BigDecimal("100000"));
        dto.setTerm(12);
        dto.setMonthlyPayment(new BigDecimal("10000"));
        dto.setRate(new BigDecimal("12.5"));
        dto.setPsk(new BigDecimal("120000"));
        dto.setIsInsuranceEnabled(true);
        dto.setIsSalaryClient(false);

        Credit result = mapper.toEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo("100000");
        assertThat(result.getTerm()).isEqualTo(12);
        assertThat(result.getInsuranceEnabled()).isTrue();

        assertThat(result.getCreditId()).isNull();
        assertThat(result.getCreditStatus()).isNull();
    }
}
