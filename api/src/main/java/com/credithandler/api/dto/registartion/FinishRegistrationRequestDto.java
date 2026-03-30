package com.credithandler.api.dto.registartion;

import com.credithandler.api.dto.calc.EmploymentDto;
import com.credithandler.api.dto.model.Gender;
import com.credithandler.api.dto.model.MaritalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FinishRegistrationRequestDto {
    @NotNull(message = "Необходимо указать пол")
    private Gender gender;

    @NotNull(message = "Необходимо указать семейное положение")
    private MaritalStatus maritalStatus;

    @NotNull(message = "Необходимо указать кол-во иждивенцев")
    @Min(value = 0, message = "Количество иждивенцев не может быть отрицательным")
    private Integer dependentAmount;

    @NotNull(message = "Необходимо указать дату выдачи паспорта")
    @Past(message = "Дата выдачи паспорта должна быть в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passportIssueDate;

    @NotBlank(message = "Необходимо указать уем был выдан паспорт")
    private String passportIssueBranch;

    @Valid
    private EmploymentDto employment;

    @NotBlank(message = "Необходимо указать номер счета")
    private String accountNumber;
}
