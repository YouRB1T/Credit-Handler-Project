package com.credithandler.api.dto.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HistoryStatusDto {
    private String status;
    private LocalDateTime timestamp;
    private ChangeType changeType;
}
