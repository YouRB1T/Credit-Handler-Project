package com.credithandler.calculator.service;

import com.credithandler.calculator.api.dto.calc.CreditDto;
import com.credithandler.calculator.api.dto.calc.ScoringDataDto;

public interface CalculateCreditService {
    CreditDto calculateCredit(ScoringDataDto request);
}
