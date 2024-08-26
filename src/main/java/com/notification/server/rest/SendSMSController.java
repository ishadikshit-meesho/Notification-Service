package com.notification.server.rest;


import com.notification.server.constants.*;
import com.notification.server.entity.Sms_requests;
import com.notification.server.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/v1")
public class SendSMSController{
    @Autowired
    private NotificationService notificationService;

    //expose sms_requests and populate the table in sql
    @PostMapping("/sms/send")
    public ResponseEntity<IntermediateResponse> sendSMS(@RequestBody SmsRequestBody body) {
        Date date = new Date();
        Sms_requests sms_request = new Sms_requests("PENDING", body.phoneNumber(), body.message(), date);
        Sms_requests dbRequest = notificationService.createSmsRecord(sms_request);
        notificationService.sendMessage(dbRequest.getId());
        IntermediateResponse response = new IntermediateResponse(String.valueOf(dbRequest.getId()),"Request is being processed, to check the status please refer to given URL", "http://localhost:8080/v1/sms/"+dbRequest.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sms/{requestId}")
    public Sms_requests getRequestId(@PathVariable String requestId) {
        Sms_requests dbRequest = notificationService.findById(requestId);
        if(dbRequest == null) {
            throw new RuntimeException("No request found with id " + requestId);
        }
        return dbRequest;
    }

    @PostMapping("/blacklist")
    public String AddNumbersToBlacklist(@RequestBody BlacklistRequestBody body) {
        return notificationService.addBlacklist(body.phoneNumbers());
    }
    @DeleteMapping("/blacklist")
    public String RemoveNumbersFromBlacklist(@RequestBody BlacklistRequestBody body) {
        return notificationService.removeBlacklist(body.phoneNumbers());
    }
    @GetMapping("/blacklist")
    public List<String> GetBlacklist() {
        return notificationService.getBlacklist();
    }
    @GetMapping("/blacklist/{phoneNumber}")
    public Boolean CheckBlacklist(@PathVariable String phoneNumber) {
        return notificationService.checkBlacklist(phoneNumber);
    }
    @GetMapping("/messages")
    public List<Object> GetAllMessagesWithPhoneNumber(@RequestBody GetMessagePhoneBody body) throws IOException {
        return notificationService.getAllMessagesWithPhone(body);
    }
    @GetMapping("/messages/get")
    public List<Object> GetAllMessagesWithText(@RequestBody GetMessageTextBody body) throws IOException {
        return  notificationService.getAllMessagesWithText(body);
    }
}
