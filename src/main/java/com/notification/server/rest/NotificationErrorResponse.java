package com.notification.server.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NotificationErrorResponse {
    private int status;
    private String message;
    private long timestamp;
    public NotificationErrorResponse() {}
    public NotificationErrorResponse(int status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

}
