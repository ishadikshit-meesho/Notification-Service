package com.notification.server.constants;

public record SmsRequestBody(String phoneNumber, String message) {
}
