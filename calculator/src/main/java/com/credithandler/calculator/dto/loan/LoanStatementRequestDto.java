package com.credithandler.calculator.dto.loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LoanStatementRequestDto {
    @Value("${prescoring.loan.min-amount}")
    private final Integer MIN_AMOUNT;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "${prescoring.loan.min-amount}", message = "Amount must be at least ${MIN_LOAN_AMOUNT}")
    private BigDecimal amount;

    private Integer term;

    private String firstName;

    private String lastName;

    private String middleName;

    private String email;

    private LocalDate birthdate;

    private String passportSeries;

    private String passportNumber;
}
