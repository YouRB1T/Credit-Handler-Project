package com.credithandler.deal.mapper;

import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.credithandler.api.dto.registartion.FinishRegistrationRequestDto;
import com.credithandler.deal.model.Client;
import com.credithandler.deal.model.Passport;
import com.credithandler.deal.model.Statement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
@DisplayName("Тестирование маппера скоринговых данных")

class ScoringDataMapperTest {

    private final ScoringDataMapper mapper = Mappers.getMapper(ScoringDataMapper.class);

    @Test
    @DisplayName("Маппинг в ScoringDataDto")
    void toScoringDataDto_shouldMapAllFields() {

        FinishRegistrationRequestDto request = new FinishRegistrationRequestDto();
        request.setGender(Gender.MALE);
        request.setMaritalStatus(MaritalStatus.SINGLE);
        request.setDependentAmount(1);
        request.setPassportIssueDate(LocalDate.now().minusYears(1));
        request.setPassportIssueBranch("123-456");
        request.setAccountNumber("12345678901234567890");

        Client client = new Client();
        client.setFirstName("Ivan");
        client.setLastName("Ivanov");
        client.setMiddleName("Ivanovich");
        client.setBirthdate(LocalDate.of(1990, 1, 1));

        Passport passport = new Passport();
        passport.setSeries("1234");
        passport.setNumber("567890");
        client.setPassport(passport);

        LoanOfferDto offer = new LoanOfferDto();
        offer.setRequestedAmount(new BigDecimal("100000"));
        offer.setTerm(12);
        offer.setIsInsuranceEnabled(true);
        offer.setIsSalaryClient(false);

        Statement statement = new Statement();
        statement.setAppliedOffer(offer);

        ScoringDataDto result = mapper.toScoringDataDto(request, client, statement);

        assertThat(result).isNotNull();

        assertThat(result.getAmount()).isEqualByComparingTo("100000");
        assertThat(result.getTerm()).isEqualTo(12);

        assertThat(result.getFirstName()).isEqualTo("Ivan");
        assertThat(result.getLastName()).isEqualTo("Ivanov");

        assertThat(result.getPassportSeries()).isEqualTo("1234");
        assertThat(result.getPassportNumber()).isEqualTo("567890");

        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getMaritalStatus()).isEqualTo(MaritalStatus.SINGLE);

        assertThat(result.getIsInsuranceEnabled()).isTrue();
        assertThat(result.getIsSalaryClient()).isFalse();
    }
}
