package com.credithandler.api.dto.model;

import com.credithandler.api.dto.loan.LoanOfferDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatementDto {
    private UUID statementId;
    private UUID clientId;
    private String email;
    private UUID creditId;
    private ApplicationStatus status;
    private LocalDateTime creationDate;
    private LoanOfferDto appliedOffer;
    private LocalDateTime signDate;
    private String sesCode;
    private List<HistoryStatusDto> historyStatus;

}
