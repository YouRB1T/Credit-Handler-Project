package com.credithandler.deal.model;


import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passport {
    private UUID passportId;
    private String series;
    private String number;
    private LocalDate issueDate;
    private String issueBranch;
}