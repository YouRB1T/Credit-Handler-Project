package com.credithandler.calculator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "scoring")
public class ScoringProperties {

    // scoring.salary.max-multiple
    private Integer maxSalaryMultiple;

    // scoring.age.min
    private Integer minAge;

    // scoring.age.max
    private Integer maxAge;

    // scoring.experience.total-min
    private Integer minTotalWorkExperience;

    // scoring.experience.current-min
    private Integer minCurrentWorkExperience;

    // scoring.employment.self-employed-increase
    private BigDecimal selfEmployedIncrease;

    // scoring.employment.company-owner-increase
    private BigDecimal companyOwnerIncrease;

    // scoring.position.middle-manager-decrease
    private BigDecimal middleManagerDecrease;

    // scoring.position.top-manager-decrease
    private BigDecimal topManagerDecrease;

    // scoring.marital.married-decrease
    private BigDecimal marriedDecrease;

    // scoring.marital.divorced-increase
    private BigDecimal divorcedIncrease;

    // scoring.gender.female.age-min
    private Integer femaleAgeMin;

    // scoring.gender.female.age-max
    private Integer femaleAgeMax;

    // scoring.gender.female.decrease
    private BigDecimal femaleDecrease;

    // scoring.gender.male.age-min
    private Integer maleAgeMin;

    // scoring.gender.male.age-max
    private Integer maleAgeMax;

    // scoring.gender.male.decrease
    private BigDecimal maleDecrease;

    // scoring.gender.non-binary.increase
    private BigDecimal nonBinaryIncrease;
}