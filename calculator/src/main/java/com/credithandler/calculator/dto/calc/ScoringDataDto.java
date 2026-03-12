package com.credithandler.calculator.dto.calc;

import com.credithandler.calculator.annotation.AdultAge;
import com.credithandler.calculator.model.Gender;
import com.credithandler.calculator.model.MaritalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoringDataDto {

    @NotNull(message = "Сумма кредита не может быть null")
    @DecimalMin(value = "20000.00", message = "Сумма кредита должна быть не меньше 20000")
    @Digits(integer = 10, fraction = 2, message = "Сумма кредита должна быть числом и максимум 2 знаками после запятой")
    private BigDecimal amount;

    @NotNull(message = "Срок кредита не может быть null")
    @Min(value = 6, message = "Срок кредита должен быть не меньше 6 месяцев")
    private Integer term;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Имя должно содержать только латинские буквы")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 30, message = "Фамилия должна содержать от 2 до 30 символов")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Фамилия должна содержать только латинские буквы")
    private String lastName;

    @Size(min = 2, max = 30, message = "Отчество должно содержать от 2 до 30 символов")
    @Pattern(regexp = "^[A-Za-z]*$", message = "Отчество должно содержать только латинские буквы")
    private String middleName;

    @NotNull(message = "Пол не может быть не заполненным")
    private Gender gender;

    @NotNull(message = "Дата рождения не может быть не заполненной")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @AdultAge
    private LocalDate birthdate;

    @NotBlank(message = "Серия паспорта не может быть пустой")
    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта должна содержать ровно 4 цифры")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта должна содержать ровно 6 цифр")
    private String passportNumber;

    @NotNull(message = "Дата выдачи паспорта не может быть не заполненной")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Код подразделения не может быть пустым")
    private String passportIssueBranch;

    @NotNull(message = "Семейное положение не может быть не заполненным")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Количество иждивенцев не может быть не заполненным")
    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    private Integer dependentAmount;

    @NotNull(message = "Информация о трудоустройстве не может быть не заполненной")
    @Valid
    private EmploymentDto employment;

    @NotBlank(message = "Номер счета не может быть пустым")
    @Pattern(regexp = "^\\d{20}$", message = "Номер счета должен содержать 20 цифр")
    private String accountNumber;

    @NotNull(message = "Параметр страховки не может быть не заполненным")
    private Boolean isInsuranceEnabled;

    @NotNull(message = "Параметр зарплатного клиента не может быть не заполненным")
    private Boolean isSalaryClient;
}