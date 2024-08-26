package com.notification.server.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//Global Exception Handler
@ControllerAdvice
public class NotificationServiceExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<NotificationErrorResponse> handleException(Exception esc){
        NotificationErrorResponse error = new NotificationErrorResponse();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(esc.getMessage());
        error.setTimestamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
