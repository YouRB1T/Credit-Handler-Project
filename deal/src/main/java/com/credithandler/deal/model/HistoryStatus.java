package com.credithandler.deal.model;

import com.credithandler.deal.model.enums.ApplicationStatus;
import com.credithandler.deal.model.enums.ChangeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HistoryStatus {
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
    private LocalDateTime timestamp;
    private ChangeType changeType;
}
