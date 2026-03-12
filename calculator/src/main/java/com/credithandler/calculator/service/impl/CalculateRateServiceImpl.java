package com.credithandler.calculator.service.impl;

import com.credithandler.calculator.dto.calc.EmploymentDto;
import com.credithandler.calculator.dto.calc.ScoringDataDto;
import com.credithandler.calculator.model.EmploymentPosition;
import com.credithandler.calculator.model.EmploymentStatus;
import com.credithandler.calculator.model.Gender;
import com.credithandler.calculator.model.MaritalStatus;
import com.credithandler.calculator.service.CalculateRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

import com.credithandler.calculator.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class CalculateRateServiceImpl implements CalculateRateService {
    // TODO: Надо ли выносить константы в отдельный синглтон класс
    @Value("${loan.base-interest}")
    private BigDecimal BASE_RATE;

    @Value("${loan.insurance-decrease}")
    private BigDecimal IF_INSURANCE;
    @Value("${loan.salary-decrease}")
    private BigDecimal IF_SALARY;

    @Value("${scoring.salary.max-multiple}")
    private Integer MAX_SALARY_MULTIPLE;

    @Value("${scoring.age.min}")
    private Integer MIN_AGE;
    @Value("${scoring.age.max}")
    private Integer MAX_AGE;

    @Value("${scoring.experience.total-min}")
    private Integer MIN_TOTAL_WROK_EXPERIENCE;
    @Value("${scoring.experience.current-min}")
    private Integer MIN_CURRENT_WORK_EXPERIENCE;

    @Value("${scoring.employment.self-employed-increase}")
    private BigDecimal SELF_EMPLOYED_INCREASE;
    @Value("${scoring.employment.company-owner-increase}")
    private BigDecimal COMPANY_OWNER_INCREASE;

    @Value("${scoring.position.middle-manager-decrease}")
    private BigDecimal MIDDLE_MANAGER_DECREASE;
    @Value("${scoring.position.top-manager-decrease}")
    private BigDecimal TOP_MANAGER_DECREASE;

    @Value("${scoring.marital.married-decrease}")
    private BigDecimal MARRIED_DECREASE;
    @Value("${scoring.marital.divorced-increase}")
    private BigDecimal DIVORCED_INCREASE;

    @Value("${scoring.gender.female.age-min}")
    private Integer FEMALE_AGE_MIN;
    @Value("${scoring.gender.female.age-max}")
    private Integer FEMALE_AGE_MAX;
    @Value("${scoring.gender.male.age-min}")
    private Integer MALE_AGE_MIN;
    @Value("${scoring.gender.male.age-max}")
    private Integer MALE_AGE_MAX;

    @Value("${scoring.gender.female.decrease}")
    private BigDecimal FEMALE_RATE_DECREASE;
    @Value("${scoring.gender.male.decrease}")
    private BigDecimal MALE_RATE_DECREASE;
    @Value("${scoring.gender.non-binary.increase}")
    private BigDecimal NON_BINARY_RATE_INCREASE;

    @Override
    public BigDecimal calculatePrescoringRate(Boolean isInsurance, Boolean isSalary) {
        BigDecimal rate = BASE_RATE;

        if (isInsurance) {
            rate = rate.subtract(IF_INSURANCE);
        }

        if (isSalary) {
            rate = rate.subtract(IF_SALARY);
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

        if (employment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            throw BusinessException.of(
                    "Статус занятости",
                    "Клиент безработный - кредит не может быть выдан"
            );
        }

        BigDecimal annualSalary = employment.getSalary().multiply(new BigDecimal("12"));
        BigDecimal maxAllowedLoan = annualSalary.multiply(new BigDecimal(MAX_SALARY_MULTIPLE));

        if (request.getAmount().compareTo(maxAllowedLoan) > 0) {
            throw BusinessException.of(
                    "Сумма кредита",
                    String.format("Сумма кредита (%.2f) превышает максимально допустимую на основе зарплаты (%.2f)",
                            request.getAmount(), maxAllowedLoan)
            );
        }

        int age = calculateAge(request.getBirthdate());
        if (age < MIN_AGE || age > MAX_AGE) {
            throw BusinessException.of(
                    "Возраст",
                    String.format("Возраст клиента %d выходит за допустимые пределы [%d-%d]",
                            age, MIN_AGE, MAX_AGE)
            );
        }

        if (employment.getWorkExperienceTotal() < MIN_TOTAL_WROK_EXPERIENCE) {
            throw BusinessException.of(
                    "Общий стаж работы",
                    String.format("Общий стаж %d месяцев меньше требуемого %d месяцев",
                            employment.getWorkExperienceTotal(), MIN_TOTAL_WROK_EXPERIENCE)
            );
        }

        if (employment.getWorkExperienceCurrent() < MIN_CURRENT_WORK_EXPERIENCE) {
            throw BusinessException.of(
                    "Текущий стаж работы",
                    String.format("Текущий стаж %d месяцев меньше требуемого %d месяцев",
                            employment.getWorkExperienceCurrent(), MIN_CURRENT_WORK_EXPERIENCE)
            );
        }
    }

    private BigDecimal countOfEmploymentStatus(ScoringDataDto request, BigDecimal currentRate) {
        EmploymentStatus status = request.getEmployment().getEmploymentStatus();
        BigDecimal newRate = currentRate;

        if (status == null) {
            return newRate;
        }

        newRate = switch (status) {
            case SELF_EMPLOYED -> currentRate.add(SELF_EMPLOYED_INCREASE);
            case COMPANY_OWNER -> currentRate.add(COMPANY_OWNER_INCREASE);
            default -> newRate;
        };

        return newRate;
    }

    private BigDecimal countOfPosition(ScoringDataDto request, BigDecimal currentRate) {
        EmploymentPosition position = request.getEmployment().getPosition();
        BigDecimal newRate = currentRate;

        if (position == null) {
            return newRate;
        }

        newRate = switch (position) {
            case MIDDLE_MANAGER -> currentRate.subtract(MIDDLE_MANAGER_DECREASE);
            case TOP_MANAGER -> currentRate.subtract(TOP_MANAGER_DECREASE);
        };

        return newRate;
    }

    private BigDecimal countOfMaritalStatus(ScoringDataDto request, BigDecimal currentRate) {
        MaritalStatus status = request.getMaritalStatus();
        BigDecimal newRate = currentRate;

        if (status == null) {
            return newRate;
        }

        newRate = switch (status) {
            case MARRIED -> currentRate.subtract(MARRIED_DECREASE);
            case DIVORCED -> currentRate.add(DIVORCED_INCREASE);
            case SINGLE -> currentRate.add(new BigDecimal(1));
        };

        return newRate;
    }

    private BigDecimal countOfGenderAndAge(ScoringDataDto request, BigDecimal currentRate) {
        Gender gender = request.getGender();
        int age = calculateAge(request.getBirthdate());
        BigDecimal newRate = currentRate;

        if (gender == null) {
            return newRate;
        }

        switch (gender) {
            case FEMALE:
                if (age >= FEMALE_AGE_MIN && age <= FEMALE_AGE_MAX) {
                    newRate = currentRate.subtract(FEMALE_RATE_DECREASE);
                }
                break;

            case MALE:
                if (age >= MALE_AGE_MIN && age <= MALE_AGE_MAX) {
                    newRate = currentRate.subtract(MALE_RATE_DECREASE);
                }
                break;

            case NON_BINARY:
                newRate = currentRate.add(NON_BINARY_RATE_INCREASE);
                break;
        }

        return newRate;
    }

    private BigDecimal ensureNonNegativeRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return rate;
    }

    private int calculateAge(LocalDate birthdate) {
        if (birthdate == null) {
            return 0;
        }
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}
