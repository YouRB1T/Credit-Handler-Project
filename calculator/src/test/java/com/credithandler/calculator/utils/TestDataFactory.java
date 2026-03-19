package com.credithandler.calculator.utils;

import com.credithandler.api.controller.calculator.dto.calc.EmploymentDto;
import com.credithandler.api.controller.calculator.dto.calc.ScoringDataDto;
import com.credithandler.api.controller.calculator.dto.loan.LoanStatementRequestDto;
import com.credithandler.api.controller.calculator.dto.model.EmploymentPosition;
import com.credithandler.api.controller.calculator.dto.model.EmploymentStatus;
import com.credithandler.api.controller.calculator.dto.model.Gender;
import com.credithandler.api.controller.calculator.dto.model.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestDataFactory {

    public static LoanStatementRequestDto createValidLoanStatementRequest() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setAmount(new BigDecimal("100000"));
        request.setTerm(12);
        request.setFirstName("Ivan");
        request.setLastName("Petrov");
        request.setMiddleName("Ivanovich");
        request.setEmail("ivan@example.com");
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");

        return request;
    }

    public static LoanStatementRequestDto createEmptyLoanStatementRequest() {
        return new LoanStatementRequestDto();
    }

    public static ScoringDataDto createValidScoringData() {
        ScoringDataDto request = new ScoringDataDto();
        request.setAmount(new BigDecimal("500000"));
        request.setTerm(12);
        request.setFirstName("Ivan");
        request.setLastName("Petrov");
        request.setMiddleName("Ivanovich");
        request.setGender(Gender.MALE);
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");
        request.setPassportIssueDate(LocalDate.of(2010, 1, 1));
        request.setPassportIssueBranch("Отдел УФМС");
        request.setMaritalStatus(MaritalStatus.MARRIED);
        request.setDependentAmount(2);
        request.setEmployment(createValidEmployment());
        request.setAccountNumber("40817810000000000001");
        request.setIsInsuranceEnabled(true);
        request.setIsSalaryClient(true);
        return request;
    }

    public static EmploymentDto createValidEmployment() {
        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setSalary(new BigDecimal("50000"));
        employment.setWorkExperienceTotal(60);
        employment.setWorkExperienceCurrent(24);
        employment.setEmployerINN("770123456789");
        employment.setPosition(EmploymentPosition.MIDDLE_MANAGER);
        return employment;
    }

    public static ScoringDataDto createScoringDataWithUnemployed() {
        ScoringDataDto request = createValidScoringData();
        EmploymentDto employment = createValidEmployment();
        employment.setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        request.setEmployment(employment);
        return request;
    }

    public static ScoringDataDto createScoringDataWithHighAmount() {
        ScoringDataDto request = createValidScoringData();
        request.setAmount(new BigDecimal("20000000"));
        return request;
    }
    public static ScoringDataDto createScoringDataWithInvalidAge(int age) {
        ScoringDataDto request = createValidScoringData();
        request.setBirthdate(LocalDate.now().minusYears(age));
        return request;
    }
}
