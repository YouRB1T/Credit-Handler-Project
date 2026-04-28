package com.credithandler.api.dto.dossier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SesCodeRequestDto {

    @NotBlank(message = "SES-код обязателен для заполнения")
    @Pattern(regexp = "\\d{4}", message = "SES-код должен состоять из 4 цифр")
    @Schema(description = "Код подтверждения, который клиент получил на email", example = "1234")
    private String sesCode;
}
