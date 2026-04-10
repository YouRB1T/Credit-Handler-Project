package com.credithandler.calculator.service;

import com.credithandler.api.controller.calculator.dto.calc.CreditDto;
import com.credithandler.api.controller.calculator.dto.calc.ScoringDataDto;

public interface CalculateCreditService {
    CreditDto calculateCredit(ScoringDataDto request);
}
