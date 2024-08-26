package com.notification.server.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.notification.server.entity.Sms_requests;
import com.notification.server.rest.SendSMSController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Service
public class ConsumerService {
    private final SendSMSController sendSMSController;
//    private final ApiService apiService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    public ConsumerService(SendSMSController sendSMSController) {
        this.sendSMSController = sendSMSController;
//        this.apiService = new ApiService();
    }
    @KafkaListener(topics = "request_ids", groupId = "group_id")
    public void consume(String id) throws IOException {
        System.out.println("Reading request ID: "+ id);
        Sms_requests updatedRequest;
        Sms_requests request = sendSMSController.getRequestId(id);
        boolean blacklisted = sendSMSController.CheckBlacklist(request.getPhone_number());
        if (blacklisted) {
            // Handle blacklisting
            System.out.println("Blacklisted: " + request.getPhone_number());
            updatedRequest = updateDb(request, "BLACKLISTED", "Phone Number has been blacklisted");
        }
        else {
            // Call third-party API to send SMS
            String response = sendSmsToThirdParty(request);
            String status = "SENT";
            updatedRequest = updateDb(request, status, null);
        }
        IndexResponse response = elasticsearchClient.index(i -> i
                .index("requests")
                .id(String.valueOf(updatedRequest.getId()))
                .document(updatedRequest)
        );
        System.out.println("Index Response: " + response);
    }

    private String sendSmsToThirdParty(Sms_requests request) {
        return "message sent";
    }

    private Sms_requests updateDb(Sms_requests request, String status, String failureDetails) {
        Date date = new Date();
        request.setStatus(status);
        request.setUpdated_at(date);
        if(!Objects.equals(status, "SENT")){
            request.setFailure_code(500);
        }
        else{
            request.setFailure_code(0);
        }
        request.setFailure_comments(failureDetails);
        return notificationService.updateSmsRecord(request);
    }
}
