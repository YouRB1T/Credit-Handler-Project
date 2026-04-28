package com.credithandler.api.dto.dossier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {
    private String address;
    private EmailTheme theme;
    private UUID statementId;
    private String text;
}
