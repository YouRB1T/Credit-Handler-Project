package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.config.LoanProperties;
import com.credithandler.calculator.config.ScoringProperties;
import com.credithandler.calculator.dto.calc.EmploymentDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.exception.BusinessException;
import com.credithandler.calculator.model.*;
import com.credithandler.calculator.service.CalculateRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateRateServiceImpl implements CalculateRateService {

    private final LoanProperties loanProperties;
    private final ScoringProperties scoringProperties;

    @Override
    public BigDecimal calculatePrescoringRate(Boolean isInsurance, Boolean isSalary) {
        log.info("Формирование процентной ставки для прескоринга");
        BigDecimal rate = loanProperties.getBaseInterest();

        if (isInsurance) {
            rate = rate.subtract(loanProperties.getInsuranceDecrease());
        }

        if (isSalary) {
            rate = rate.subtract(loanProperties.getSalaryDecrease());
        }

        rate = ensureNonNegativeRate(rate);

        return rate;
    }

    @Override
    public BigDecimal calculateScoringRate(ScoringDataDto scoringData) {
        instantRejection(scoringData);

        BigDecimal rate = calculatePrescoringRate(
                scoringData.getIsInsuranceEnabled(),
                scoringData.getIsSalaryClient()
        );

        rate = countOfEmploymentStatus(scoringData, rate);
        rate = countOfPosition(scoringData, rate);
        rate = countOfMaritalStatus(scoringData, rate);
        rate = countOfGenderAndAge(scoringData, rate);
        rate = ensureNonNegativeRate(rate);

        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    private void instantRejection(ScoringDataDto request) {
        EmploymentDto employment = request.getEmployment();
        ScoringProperties props = scoringProperties;

        if (employment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            throw BusinessException.of(
                    "Статус занятости",
                    "Клиент безработный - кредит не может быть выдан"
            );
        }

        BigDecimal annualSalary = employment.getSalary().multiply(new BigDecimal("12"));
        BigDecimal maxAllowedLoan = annualSalary.multiply(new BigDecimal(props.getMaxSalaryMultiple()));

        if (request.getAmount().compareTo(maxAllowedLoan) > 0) {
            throw BusinessException.of(
                    "Сумма кредита",
                    String.format("Сумма кредита (%.2f) превышает максимально допустимую на основе зарплаты (%.2f)",
                            request.getAmount(), maxAllowedLoan)
            );
        }

        int age = calculateAge(request.getBirthdate());
        if (age < props.getMinAge() || age > props.getMaxAge()) {
            throw BusinessException.of(
                    "Возраст",
                    String.format("Возраст клиента %d выходит за допустимые пределы [%d-%d]",
                            age, props.getMinAge(), props.getMaxAge())
            );
        }

        if (employment.getWorkExperienceTotal() < props.getMinTotalWorkExperience()) {
            throw BusinessException.of(
                    "Общий стаж работы",
                    String.format("Общий стаж %d месяцев меньше требуемого %d месяцев",
                            employment.getWorkExperienceTotal(), props.getMinTotalWorkExperience())
            );
        }

        if (employment.getWorkExperienceCurrent() < props.getMinCurrentWorkExperience()) {
            throw BusinessException.of(
                    "Текущий стаж работы",
                    String.format("Текущий стаж %d месяцев меньше требуемого %d месяцев",
                            employment.getWorkExperienceCurrent(), props.getMinCurrentWorkExperience())
            );
        }
    }

    private BigDecimal countOfEmploymentStatus(ScoringDataDto request, BigDecimal currentRate) {
        EmploymentStatus status = request.getEmployment().getEmploymentStatus();
        ScoringProperties props = scoringProperties;

        if (status == null) {
            return currentRate;
        }

        return switch (status) {
            case SELF_EMPLOYED -> currentRate.add(props.getSelfEmployedIncrease());
            case COMPANY_OWNER -> currentRate.add(props.getCompanyOwnerIncrease());
            default -> currentRate;
        };
    }

    private BigDecimal countOfPosition(ScoringDataDto request, BigDecimal currentRate) {
        EmploymentPosition position = request.getEmployment().getPosition();
        ScoringProperties props = scoringProperties;

        if (position == null) {
            return currentRate;
        }

        return switch (position) {
            case MIDDLE_MANAGER -> currentRate.subtract(props.getMiddleManagerDecrease());
            case TOP_MANAGER -> currentRate.subtract(props.getTopManagerDecrease());
        };
    }

    private BigDecimal countOfMaritalStatus(ScoringDataDto request, BigDecimal currentRate) {
        MaritalStatus status = request.getMaritalStatus();
        ScoringProperties props = scoringProperties;

        if (status == null) {
            return currentRate;
        }

        return switch (status) {
            case MARRIED -> currentRate.subtract(props.getMarriedDecrease());
            case DIVORCED -> currentRate.add(props.getDivorcedIncrease());
            case SINGLE -> currentRate.add(new BigDecimal("1"));
        };
    }

    private BigDecimal countOfGenderAndAge(ScoringDataDto request, BigDecimal currentRate) {
        Gender gender = request.getGender();
        int age = calculateAge(request.getBirthdate());
        ScoringProperties props = scoringProperties;

        if (gender == null) {
            return currentRate;
        }

        switch (gender) {
            case FEMALE:
                if (age >= props.getFemaleAgeMin() && age <= props.getFemaleAgeMax()) {
                    currentRate = currentRate.subtract(props.getFemaleDecrease());
                }
                break;

            case MALE:
                if (age >= props.getMaleAgeMin() && age <= props.getMaleAgeMax()) {
                    currentRate = currentRate.subtract(props.getMaleDecrease());
                }
                break;

            case NON_BINARY:
                currentRate = currentRate.add(props.getNonBinaryIncrease());
                break;
        }

        return currentRate;
    }

    private BigDecimal ensureNonNegativeRate(BigDecimal rate) {
        return rate.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : rate;
    }

    private int calculateAge(LocalDate birthdate) {
        if (birthdate == null) {
            return 0;
        }
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}