package com.notification.server.constants;


public record GetMessagePhoneBody(String phoneNumber, String startTime, String endTime, int from, int size) {
}
