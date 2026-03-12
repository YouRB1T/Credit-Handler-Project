package com.credithandler.calculator.annotation;

import com.credithandler.calculator.validator.AdultAgeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultAgeValidator.class)
@Documented
public @interface AdultAge {
    String message() default "Клиент должен быть совершеннолетним.";
    int minAge() default 18;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
