package com.notification.server.service;

import com.notification.server.entity.Sms_requests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class ApiService {
    @Autowired
    private RestTemplate restTemplate;
    public String send(Sms_requests request) {
        String url = "https://api.imiconnect.in/resources/v1/messaging";
        String correlationId = UUID.randomUUID() + "-" + System.currentTimeMillis();

        String jsonRequestBody = String.format(
                """
                        [
                          {
                            "deliverychannel": "sms",
                            "channels": {
                              "sms": {
                                "text": "%s"
                              }
                            },
                            "destination": [
                              {
                                "msisdn": [
                                   "%s"
                                ],
                                "correlationId": "%s"
                              }
                            ]
                          }
                        ]""",
                request.getMessage(),
                request.getPhone_number(),
                correlationId
        );

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Set up the request entity with headers and body
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

        // Make the API call
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Return the response body
        return response.getBody();
    }
}
