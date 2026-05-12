package com.credithandler.calculator.service.impl;

import com.credithandler.api.dto.error.BusinessException;
import com.credithandler.api.dto.model.EmploymentPosition;
import com.credithandler.api.dto.model.EmploymentStatus;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.credithandler.calculator.config.ErrorProperties;
import com.credithandler.calculator.config.LoanProperties;
import com.credithandler.calculator.config.ScoringProperties;
import com.credithandler.api.dto.calc.EmploymentDto;
import com.credithandler.api.dto.calc.ScoringDataDto;
import com.credithandler.calculator.constants.ErrorMessages;
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
    private final ErrorProperties errorProperties;

    private static final int RATE_SCALE = 2;
    private static final BigDecimal TWELVE = new BigDecimal("12");
    private static final int MIN_RATE = 0;

    @Override
    public BigDecimal calculatePrescoringRate(Boolean isInsurance, Boolean isSalary) {
        log.info(">> calculatePrescoringRate, isInsurance: {}, isSalary: {}", isInsurance, isSalary);

        BigDecimal rate = loanProperties.getBaseInterest();
        log.debug("Базовая ставка: {}%", rate);

        if (isInsurance) {
            rate = rate.subtract(loanProperties.getInsuranceDecrease());
            log.debug("Применена скидка за страхование: -{}%", loanProperties.getInsuranceDecrease());
        }

        if (isSalary) {
            rate = rate.subtract(loanProperties.getSalaryDecrease());
            log.debug("Применена скидка за зарплатный проект: -{}%", loanProperties.getSalaryDecrease());
        }

        rate = ensureNonNegativeRate(rate);

        log.info("<< calculatePrescoringRate, result: {}%", rate);
        return rate;
    }

    @Override
    public BigDecimal calculateScoringRate(ScoringDataDto scoringData) {
        log.info(">> calculateScoringRate, scoringData: {}", scoringData);

        instantRejection(scoringData);

        BigDecimal rate = calculatePrescoringRate(
                scoringData.getIsInsuranceEnabled(),
                scoringData.getIsSalaryClient()
        );
        log.debug("Ставка после прескоринга: {}%", rate);

        rate = countOfEmploymentStatus(scoringData, rate);
        rate = countOfPosition(scoringData, rate);
        rate = countOfMaritalStatus(scoringData, rate);
        rate = countOfGenderAndAge(scoringData, rate);
        rate = ensureNonNegativeRate(rate);

        BigDecimal finalRate = rate.setScale(RATE_SCALE, RoundingMode.HALF_UP);

        log.info("<< calculateScoringRate, result: {}%", finalRate);
        return finalRate;
    }

    private void instantRejection(ScoringDataDto request) {
        EmploymentDto employment = request.getEmployment();
        ScoringProperties props = scoringProperties;

        log.debug("Проверка критериев для мгновенного отказа");

        if (employment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            log.warn("Отказ: {}", ErrorMessages.ERROR_LOG_EMPLOYMENT_STATUS);
            throw BusinessException.of(
                    ErrorMessages.ERROR_LOG_EMPLOYMENT_STATUS,
                    ErrorMessages.ERROR_UNEMPLOYED_MESSAGE
            );
        }

        BigDecimal annualSalary = employment.getSalary().multiply(TWELVE);
        BigDecimal maxAllowedLoan = annualSalary.multiply(new BigDecimal(props.getMaxSalaryMultiple()));

        if (request.getAmount().compareTo(maxAllowedLoan) > 0) {
            log.warn("Отказ: {}", ErrorMessages.ERROR_LOG_LOAN_AMOUNT);
            throw BusinessException.of(
                    ErrorMessages.ERROR_LOG_LOAN_AMOUNT,
                    String.format(ErrorMessages.ERROR_LOAN_AMOUNT_MESSAGE + " (%.2f > %.2f)",
                            request.getAmount(), maxAllowedLoan)
            );
        }

        int age = calculateAge(request.getBirthdate());
        if (age < props.getMinAge() || age > props.getMaxAge()) {
            log.warn("Отказ: {}", ErrorMessages.ERROR_LOG_AGE);
            throw BusinessException.of(
                    ErrorMessages.ERROR_LOG_AGE,
                    String.format(ErrorMessages.ERROR_AGE_MESSAGE + " [%d-%d], текущий: %d",
                            props.getMinAge(), props.getMaxAge(), age)
            );
        }

        if (employment.getWorkExperienceTotal() < props.getMinTotalWorkExperience()) {
            log.warn("Отказ: {}", ErrorMessages.ERROR_LOG_TOTAL_EXPERIENCE);
            throw BusinessException.of(
                    ErrorMessages.ERROR_LOG_TOTAL_EXPERIENCE,
                    String.format(ErrorMessages.ERROR_TOTAL_EXPERIENCE_MESSAGE + " %d < %d",
                            employment.getWorkExperienceTotal(), props.getMinTotalWorkExperience())
            );
        }

        if (employment.getWorkExperienceCurrent() < props.getMinCurrentWorkExperience()) {
            log.warn("Отказ: {}", ErrorMessages.ERROR_LOG_CURRENT_EXPERIENCE);
            throw BusinessException.of(
                    ErrorMessages.ERROR_LOG_CURRENT_EXPERIENCE,
                    String.format(ErrorMessages.ERROR_CURRENT_EXPERIENCE_MESSAGE + " %d < %d",
                            employment.getWorkExperienceCurrent(), props.getMinCurrentWorkExperience())
            );
        }
    }

    private BigDecimal countOfEmploymentStatus(ScoringDataDto request, BigDecimal currentRate) {
        EmploymentStatus status = request.getEmployment().getEmploymentStatus();
        ScoringProperties props = scoringProperties;

        BigDecimal newRate = switch (status) {
            case SELF_EMPLOYED -> {
                log.debug(ErrorMessages.ERROR_LOG_SELF_EMPLOYED, props.getSelfEmployedIncrease());
                yield currentRate.add(props.getSelfEmployedIncrease());
            }
            case COMPANY_OWNER -> {
                log.debug(ErrorMessages.ERROR_LOG_COMPANY_OWNER, props.getCompanyOwnerIncrease());
                yield currentRate.add(props.getCompanyOwnerIncrease());
            }
            default -> currentRate;
        };

        log.debug("Статус занятости: {}, ставка после учета: {}%", status, newRate);
        return newRate;
    }

    private BigDecimal countOfPosition(ScoringDataDto request, BigDecimal currentRate) {
        EmploymentPosition position = request.getEmployment().getPosition();
        ScoringProperties props = scoringProperties;

        BigDecimal newRate = switch (position) {
            case MIDDLE_MANAGER -> {
                log.debug(ErrorMessages.ERROR_LOG_MIDDLE_MANAGER, props.getMiddleManagerDecrease());
                yield currentRate.subtract(props.getMiddleManagerDecrease());
            }
            case TOP_MANAGER -> {
                log.debug(ErrorMessages.ERROR_LOG_TOP_MANAGER, props.getTopManagerDecrease());
                yield currentRate.subtract(props.getTopManagerDecrease());
            }
            default -> currentRate;
        };

        log.debug("Должность: {}, ставка после учета: {}%", position, newRate);
        return newRate;
    }

    private BigDecimal countOfMaritalStatus(ScoringDataDto request, BigDecimal currentRate) {
        MaritalStatus status = request.getMaritalStatus();
        ScoringProperties props = scoringProperties;

        BigDecimal newRate = switch (status) {
            case MARRIED -> {
                log.debug(ErrorMessages.ERROR_LOG_MARRIED, props.getMarriedDecrease());
                yield currentRate.subtract(props.getMarriedDecrease());
            }
            case DIVORCED -> {
                log.debug(ErrorMessages.ERROR_LOG_DIVORCED, props.getDivorcedIncrease());
                yield currentRate.add(props.getDivorcedIncrease());
            }
            default -> currentRate;
        };

        log.debug("Семейное положение: {}, ставка после учета: {}%", status, newRate);
        return newRate;
    }

    private BigDecimal countOfGenderAndAge(ScoringDataDto request, BigDecimal currentRate) {
        Gender gender = request.getGender();
        int age = calculateAge(request.getBirthdate());
        ScoringProperties props = scoringProperties;

        BigDecimal newRate = currentRate;

        switch (gender) {
            case FEMALE:
                if (age >= props.getFemaleAgeMin() && age <= props.getFemaleAgeMax()) {
                    newRate = currentRate.subtract(props.getFemaleDecrease());
                    log.debug(ErrorMessages.ERROR_LOG_FEMALE_DISCOUNT,
                            props.getFemaleAgeMin(), props.getFemaleAgeMax(),
                            props.getFemaleDecrease());
                }
                break;

            case MALE:
                if (age >= props.getMaleAgeMin() && age <= props.getMaleAgeMax()) {
                    newRate = currentRate.subtract(props.getMaleDecrease());
                    log.debug(ErrorMessages.ERROR_LOG_MALE_DISCOUNT,
                            props.getMaleAgeMin(), props.getMaleAgeMax(),
                            props.getMaleDecrease());
                }
                break;

            case NON_BINARY:
                newRate = currentRate.add(props.getNonBinaryIncrease());
                log.debug(ErrorMessages.ERROR_LOG_NON_BINARY_INCREASE, props.getNonBinaryIncrease());
                break;
        }

        log.debug("Пол: {}, возраст: {}, ставка после учета: {}%", gender, age, newRate);
        return newRate;
    }

    private BigDecimal ensureNonNegativeRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < MIN_RATE) {
            log.debug("Ставка была отрицательной ({}), установлена в 0%", rate);
            return BigDecimal.ZERO;
        }
        return rate;
    }

    private int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }
}