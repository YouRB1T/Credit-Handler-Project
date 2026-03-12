package com.credithandler.calculator.service;

import com.credithandler.calculator.dto.calc.ScoringDataDto;

import java.math.BigDecimal;

public interface CalculateRateService {

    BigDecimal calculatePrescoringRate(Boolean isInsuranceEnabled, Boolean isSalaryClient);

    BigDecimal calculateScoringRate(ScoringDataDto scoringData);
}
