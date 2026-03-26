package com.credithandler.deal.model;

import com.credithandler.api.dto.loan.LoanOfferDto;
import com.credithandler.deal.model.enums.ApplicationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Statement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID statementId;

    private UUID clientId;
    private UUID creditId;
    private ApplicationStatus status;
    private LocalDateTime creationDate;
    @JdbcTypeCode(SqlTypes.JSON)
    private List<LoanOfferDto> appliedOffers;
    private LocalDateTime signDate;
    private String sesCode;
    @JdbcTypeCode(SqlTypes.JSON)
    private HistoryStatus historyStatus;

}
