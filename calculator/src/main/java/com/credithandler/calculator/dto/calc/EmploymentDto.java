package com.credithandler.calculator.dto.calc;

import com.credithandler.calculator.model.EmploymentPosition;
import com.credithandler.calculator.model.EmploymentStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentDto {

    @NotNull(message = "Статус занятости не может быть не заполненным")
    private EmploymentStatus employmentStatus;

    @NotBlank(message = "ИНН работодателя не может быть пустым")
    @Pattern(
            regexp = "^\\d{10}$|^\\d{12}$",
            message = "ИНН должен содержать 10 или 12 цифр"
    )
    private String employerINN;

    @NotNull(message = "Зарплата не может быть не заполненным")
    @DecimalMin(value = "0.01", message = "Зарплата должна быть больше 0")
    @Digits(integer = 10, fraction = 2, message = "Зарплата должна быть числом с максимум 2 знаками после запятой")
    private BigDecimal salary;

    @NotNull(message = "Должность не может быть null")
    private EmploymentPosition position;

    @NotNull(message = "Общий стаж не может быть не заполненным")
    @Min(value = 0, message = "Общий стаж не может быть отрицательным")
    private Integer workExperienceTotal;

    @NotNull(message = "Текущий стаж не может быть не заполненным")
    @Min(value = 0, message = "Текущий стаж не может быть отрицательным")
    private Integer workExperienceCurrent;
}
