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
        log.info("Расчет ставки для прескоринга: страхование {}, зарплатный клиент {}",
                isInsurance, isSalary);

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
        log.info("Итоговая ставка прескоринга: {}%", rate);

        return rate;
    }

    @Override
    public BigDecimal calculateScoringRate(ScoringDataDto scoringData) {
        log.info("Начало скоринга для клиента: пол {}, возраст {} лет",
                scoringData.getGender(), calculateAge(scoringData.getBirthdate()));

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

        BigDecimal finalRate = rate.setScale(2, RoundingMode.HALF_UP);
        log.info("Скоринг завершен. Итоговая ставка: {}%", finalRate);

        return finalRate;
    }

    private void instantRejection(ScoringDataDto request) {
        EmploymentDto employment = request.getEmployment();
        ScoringProperties props = scoringProperties;

        log.debug("Проверка критериев для мгновенного отказа");

        if (employment.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            log.warn("Отказ: клиент безработный");
            throw BusinessException.of(
                    "Статус занятости",
                    "Клиент безработный - кредит не может быть выдан"
            );
        }

        BigDecimal annualSalary = employment.getSalary().multiply(new BigDecimal("12"));
        BigDecimal maxAllowedLoan = annualSalary.multiply(new BigDecimal(props.getMaxSalaryMultiple()));

        if (request.getAmount().compareTo(maxAllowedLoan) > 0) {
            log.warn("Отказ: сумма кредита {} превышает допустимую {}",
                    request.getAmount(), maxAllowedLoan);
            throw BusinessException.of(
                    "Сумма кредита",
                    String.format("Сумма кредита (%.2f) превышает максимально допустимую на основе зарплаты (%.2f)",
                            request.getAmount(), maxAllowedLoan)
            );
        }

        int age = calculateAge(request.getBirthdate());
        if (age < props.getMinAge() || age > props.getMaxAge()) {
            log.warn("Отказ: возраст {} вне допустимого диапазона [{}-{}]",
                    age, props.getMinAge(), props.getMaxAge());
            throw BusinessException.of(
                    "Возраст",
                    String.format("Возраст клиента %d выходит за допустимые пределы [%d-%d]",
                            age, props.getMinAge(), props.getMaxAge())
            );
        }

        if (employment.getWorkExperienceTotal() < props.getMinTotalWorkExperience()) {
            log.warn("Отказ: общий стаж {} мес. меньше требуемого {} мес.",
                    employment.getWorkExperienceTotal(), props.getMinTotalWorkExperience());
            throw BusinessException.of(
                    "Общий стаж работы",
                    String.format("Общий стаж %d месяцев меньше требуемого %d месяцев",
                            employment.getWorkExperienceTotal(), props.getMinTotalWorkExperience())
            );
        }

        if (employment.getWorkExperienceCurrent() < props.getMinCurrentWorkExperience()) {
            log.warn("Отказ: текущий стаж {} мес. меньше требуемого {} мес.",
                    employment.getWorkExperienceCurrent(), props.getMinCurrentWorkExperience());
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

        BigDecimal newRate = switch (status) {
            case SELF_EMPLOYED -> {
                log.debug("Применено повышение ставки для самозанятого: +{}%",
                        props.getSelfEmployedIncrease());
                yield currentRate.add(props.getSelfEmployedIncrease());
            }
            case COMPANY_OWNER -> {
                log.debug("Применено повышение ставки для владельца бизнеса: +{}%",
                        props.getCompanyOwnerIncrease());
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

        if (position == null) {
            return currentRate;
        }

        BigDecimal newRate = switch (position) {
            case MIDDLE_MANAGER -> {
                log.debug("Применена скидка для руководителя среднего звена: -{}%",
                        props.getMiddleManagerDecrease());
                yield currentRate.subtract(props.getMiddleManagerDecrease());
            }
            case TOP_MANAGER -> {
                log.debug("Применена скидка для топ-менеджера: -{}%",
                        props.getTopManagerDecrease());
                yield currentRate.subtract(props.getTopManagerDecrease());
            }
        };

        log.debug("Должность: {}, ставка после учета: {}%", position, newRate);
        return newRate;
    }

    private BigDecimal countOfMaritalStatus(ScoringDataDto request, BigDecimal currentRate) {
        MaritalStatus status = request.getMaritalStatus();
        ScoringProperties props = scoringProperties;

        if (status == null) {
            return currentRate;
        }

        BigDecimal newRate = switch (status) {
            case MARRIED -> {
                log.debug("Применена скидка для женатых/замужем: -{}%", props.getMarriedDecrease());
                yield currentRate.subtract(props.getMarriedDecrease());
            }
            case DIVORCED -> {
                log.debug("Применено повышение ставки для разведенных: +{}%", props.getDivorcedIncrease());
                yield currentRate.add(props.getDivorcedIncrease());
            }
            case SINGLE -> {
                log.debug("Применено повышение ставки для холостых/незамужних: +1%");
                yield currentRate.add(new BigDecimal("1"));
            }
        };

        log.debug("Семейное положение: {}, ставка после учета: {}%", status, newRate);
        return newRate;
    }

    private BigDecimal countOfGenderAndAge(ScoringDataDto request, BigDecimal currentRate) {
        Gender gender = request.getGender();
        int age = calculateAge(request.getBirthdate());
        ScoringProperties props = scoringProperties;

        if (gender == null) {
            return currentRate;
        }

        BigDecimal newRate = currentRate;

        switch (gender) {
            case FEMALE:
                if (age >= props.getFemaleAgeMin() && age <= props.getFemaleAgeMax()) {
                    newRate = currentRate.subtract(props.getFemaleDecrease());
                    log.debug("Применена скидка для женщины {}-{} лет: -{}%",
                            props.getFemaleAgeMin(), props.getFemaleAgeMax(),
                            props.getFemaleDecrease());
                }
                break;

            case MALE:
                if (age >= props.getMaleAgeMin() && age <= props.getMaleAgeMax()) {
                    newRate = currentRate.subtract(props.getMaleDecrease());
                    log.debug("Применена скидка для мужчины {}-{} лет: -{}%",
                            props.getMaleAgeMin(), props.getMaleAgeMax(),
                            props.getMaleDecrease());
                }
                break;

            case NON_BINARY:
                newRate = currentRate.add(props.getNonBinaryIncrease());
                log.debug("Применено повышение ставки для небинарных: +{}%", props.getNonBinaryIncrease());
                break;
        }

        log.debug("Пол: {}, возраст: {}, ставка после учета: {}%", gender, age, newRate);
        return newRate;
    }

    private BigDecimal ensureNonNegativeRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) < 0) {
            log.debug("Ставка была отрицательной ({}), установлена в 0%", rate);
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