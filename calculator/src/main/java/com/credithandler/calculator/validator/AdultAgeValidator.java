package com.credithandler.calculator.validator;

import com.credithandler.calculator.annotation.AdultAge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class AdultAgeValidator implements ConstraintValidator<AdultAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(AdultAge annotation) {
        this.minAge = annotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate birthdate, ConstraintValidatorContext context) {
        if (birthdate == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        int age = Period.between(birthdate, today).getYears();

        return age >= minAge;
    }
}