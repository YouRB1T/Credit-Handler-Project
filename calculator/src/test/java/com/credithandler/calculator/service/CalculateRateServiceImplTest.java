package com.credithandler.calculator.service;

import com.credithandler.calculator.dto.calc.EmploymentDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.exception.BusinessException;
import com.credithandler.calculator.model.*;
import com.credithandler.calculator.service.impl.CalculateRateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalculateRateServiceImplTest {

    @InjectMocks
    private CalculateRateServiceImpl calculateRateService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(calculateRateService, "BASE_RATE", new BigDecimal("10"));
        ReflectionTestUtils.setField(calculateRateService, "IF_INSURANCE", new BigDecimal("2"));
        ReflectionTestUtils.setField(calculateRateService, "IF_SALARY", new BigDecimal("1"));
        ReflectionTestUtils.setField(calculateRateService, "MAX_SALARY_MULTIPLE", 24);
        ReflectionTestUtils.setField(calculateRateService, "MIN_AGE", 20);
        ReflectionTestUtils.setField(calculateRateService, "MAX_AGE", 65);
        ReflectionTestUtils.setField(calculateRateService, "MIN_TOTAL_WROK_EXPERIENCE", 18);
        ReflectionTestUtils.setField(calculateRateService, "MIN_CURRENT_WORK_EXPERIENCE", 3);
        ReflectionTestUtils.setField(calculateRateService, "SELF_EMPLOYED_INCREASE", new BigDecimal("2"));
        ReflectionTestUtils.setField(calculateRateService, "COMPANY_OWNER_INCREASE", new BigDecimal("1"));
        ReflectionTestUtils.setField(calculateRateService, "MIDDLE_MANAGER_DECREASE", new BigDecimal("2"));
        ReflectionTestUtils.setField(calculateRateService, "TOP_MANAGER_DECREASE", new BigDecimal("3"));
        ReflectionTestUtils.setField(calculateRateService, "MARRIED_DECREASE", new BigDecimal("3"));
        ReflectionTestUtils.setField(calculateRateService, "DIVORCED_INCREASE", new BigDecimal("1"));
        ReflectionTestUtils.setField(calculateRateService, "FEMALE_AGE_MIN", 32);
        ReflectionTestUtils.setField(calculateRateService, "FEMALE_AGE_MAX", 60);
        ReflectionTestUtils.setField(calculateRateService, "MALE_AGE_MIN", 30);
        ReflectionTestUtils.setField(calculateRateService, "MALE_AGE_MAX", 55);
        ReflectionTestUtils.setField(calculateRateService, "FEMALE_RATE_DECREASE", new BigDecimal("3"));
        ReflectionTestUtils.setField(calculateRateService, "MALE_RATE_DECREASE", new BigDecimal("3"));
        ReflectionTestUtils.setField(calculateRateService, "NON_BINARY_RATE_INCREASE", new BigDecimal("7"));
    }

    @Test
    void calculateScoringRate_MultipleFactors_ShouldCombineCorrectly() {
        ScoringDataDto request = createValidScoringData();
        request.setIsInsuranceEnabled(true); // -2
        request.setIsSalaryClient(true);     // -1
        request.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED); // +2
        request.getEmployment().setPosition(EmploymentPosition.TOP_MANAGER); // -3
        request.setMaritalStatus(MaritalStatus.MARRIED); // -3
        request.setGender(Gender.MALE);
        request.setBirthdate(LocalDate.now().minusYears(40)); // -3

        BigDecimal result = calculateRateService.calculateScoringRate(request);
        // 10 -2 -1 +2 -3 -3 -3 = 0
        assertEquals(new BigDecimal("0.00"), result);
    }


    private ScoringDataDto createValidScoringData() {
        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setSalary(new BigDecimal("50000"));
        employment.setPosition(EmploymentPosition.MIDDLE_MANAGER);
        employment.setWorkExperienceTotal(60);
        employment.setWorkExperienceCurrent(24);

        ScoringDataDto scoringData = new ScoringDataDto();
        scoringData.setAmount(new BigDecimal("200000"));
        scoringData.setTerm(12);
        scoringData.setFirstName("John");
        scoringData.setLastName("Doe");
        scoringData.setGender(Gender.MALE);
        scoringData.setBirthdate(LocalDate.now().minusYears(30));
        scoringData.setPassportSeries("1234");
        scoringData.setPassportNumber("123456");
        scoringData.setPassportIssueDate(LocalDate.now().minusYears(10));
        scoringData.setPassportIssueBranch("Test Branch");
        scoringData.setMaritalStatus(MaritalStatus.SINGLE);
        scoringData.setDependentAmount(0);
        scoringData.setEmployment(employment);
        scoringData.setAccountNumber("12345678901234567890");
        scoringData.setIsInsuranceEnabled(false);
        scoringData.setIsSalaryClient(false);

        return scoringData;
    }
}