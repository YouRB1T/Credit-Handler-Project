package com.credithandler.calculator.dto.loan;

import com.credithandler.calculator.annotation.AdultAge;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanStatementRequestDto {

    @NotNull(message = "Сумма кредите не может быть нулевой")
    @DecimalMin(value = "20000.00", message = "Сумма кредита должна быть больше 20000")
    @Digits(integer = 10, fraction = 2, message = "Число должно быть действительным, не больше 99,999,999.99 и до 2 дробной части")
    private BigDecimal amount;

    @NotNull(message = "Срок кредита не может быть нулем")
    @Min(value = 6, message = "Срок кредита не может быть меньше 6 месяцев")
    private Integer term;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 30, message = "Длина имени от 2 до 30 букв")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Имя должно включать только буквы")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 30, message = "Длина фамилии от 2 до 30 букв")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Фамилия должна включать только буквы")
    private String lastName;

    @Size(min = 2, max = 30, message = "Длина отчества от 2 до 30 символов")
    @Pattern(regexp = "^[A-Za-z]*$", message = "Отчество должно состоять только из символов")
    private String middleName;

    @NotBlank(message = "Почта не может быть пустой")
    @Pattern(
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            message = "Неверный формат почты"
    )
    private String email;

    @NotNull(message = "День рождения не может быть пустым")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    //https://www.baeldung.com/spring-mvc-custom-validator
    @AdultAge()
    private LocalDate birthdate;

    // TODO: Нужна ли проверка серии и номера паспорта по паттерну
    @NotBlank(message = "Серия паспорта не может быть пустой")
    @Pattern(regexp = "^\\d{4}$", message = "Серия паспорта состоит из 4 цифр")
    private String passportSeries;

    @NotBlank(message = "Номер паспорта не может быть пустым")
    @Pattern(regexp = "^\\d{6}$", message = "Номер паспорта состоит из 6 цифр")
    private String passportNumber;
}