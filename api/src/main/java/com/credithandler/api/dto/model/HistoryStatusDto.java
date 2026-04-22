package com.credithandler.api.dto.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
