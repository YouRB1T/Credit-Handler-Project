package com.credithandler.api.dto.model;

import java.time.LocalDateTime;

public class HistoryStatusDto {
    private String status;
    private LocalDateTime timestamp;
    private ChangeType changeType;
}
