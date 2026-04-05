package com.credithandler.api.dto.error;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BusinessErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
