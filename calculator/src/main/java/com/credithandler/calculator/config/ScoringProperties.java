package com.credithandler.calculator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "scoring")
public class ScoringProperties {

    private Integer maxSalaryMultiple;

    private Integer minAge;

    private Integer maxAge;

    private Integer minTotalWorkExperience;

    private Integer minCurrentWorkExperience;

    private BigDecimal selfEmployedIncrease;

    private BigDecimal companyOwnerIncrease;

    private BigDecimal middleManagerDecrease;

    private BigDecimal topManagerDecrease;

    private BigDecimal marriedDecrease;

    private BigDecimal divorcedIncrease;

    private Integer femaleAgeMin;

    private Integer femaleAgeMax;

    private BigDecimal femaleDecrease;

    private Integer maleAgeMin;

    private Integer maleAgeMax;

    private BigDecimal maleDecrease;

    private BigDecimal nonBinaryIncrease;
}