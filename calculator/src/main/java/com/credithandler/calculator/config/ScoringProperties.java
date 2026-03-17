package com.credithandler.calculator.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
public class ScoringProperties {

    @Value("${scoring.salary.max-multiple}")
    private Integer maxSalaryMultiple;

    @Value("${scoring.age.min}")
    private Integer minAge;

    @Value("${scoring.age.max}")
    private Integer maxAge;

    @Value("${scoring.experience.total-min}")
    private Integer minTotalWorkExperience;

    @Value("${scoring.experience.current-min}")
    private Integer minCurrentWorkExperience;

    @Value("${scoring.employment.self-employed-increase}")
    private BigDecimal selfEmployedIncrease;

    @Value("${scoring.employment.company-owner-increase}")
    private BigDecimal companyOwnerIncrease;

    @Value("${scoring.position.middle-manager-decrease}")
    private BigDecimal middleManagerDecrease;

    @Value("${scoring.position.top-manager-decrease}")
    private BigDecimal topManagerDecrease;

    @Value("${scoring.marital.married-decrease}")
    private BigDecimal marriedDecrease;

    @Value("${scoring.marital.divorced-increase}")
    private BigDecimal divorcedIncrease;

    @Value("${scoring.gender.female.age-min}")
    private Integer femaleAgeMin;

    @Value("${scoring.gender.female.age-max}")
    private Integer femaleAgeMax;

    @Value("${scoring.gender.female.decrease}")
    private BigDecimal femaleDecrease;

    @Value("${scoring.gender.female.age-min}")
    private Integer maleAgeMin;

    @Value("${scoring.gender.male.age-max}")
    private Integer maleAgeMax;

    @Value("${scoring.gender.male.decrease}")
    private BigDecimal maleDecrease;

    @Value("${scoring.gender.non-binary.increase}")
    private BigDecimal nonBinaryIncrease;
}