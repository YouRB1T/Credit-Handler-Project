package com.credithandler.deal.model;

import com.credithandler.deal.model.enums.ChangeType;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HistoryStatus {
    private String status;
    private LocalDateTime timestamp;
    private ChangeType changeType;
}
