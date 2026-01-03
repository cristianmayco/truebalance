package com.truebalance.truebalance.application.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;

    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
