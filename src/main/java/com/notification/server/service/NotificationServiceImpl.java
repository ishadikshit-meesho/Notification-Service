package com.notification.server.service;


import com.notification.server.constants.GetMessagePhoneBody;
import com.notification.server.constants.GetMessageTextBody;
import com.notification.server.dao.SmsDAO;
import com.notification.server.entity.Sms_requests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final SmsDAO smsDAO;
    private static final String TOPIC = "request_ids";
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public NotificationServiceImpl(SmsDAO smsDAO, KafkaTemplate<String, String> kafkaTemplate) {
        this.smsDAO = smsDAO;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Override
    @Transactional
    public Sms_requests createSmsRecord(Sms_requests sms_requests) {
        return smsDAO.createSmsRecord(sms_requests);
    }
    @Override
    public Sms_requests findById(String id) {
        return smsDAO.findById(id);
    }

    @Override
    @Transactional
    public String addBlacklist(String[] phoneNumbers) {
        return smsDAO.addBlacklist(phoneNumbers);
    }

    @Override
    @Transactional
    public String removeBlacklist(String[] phoneNumbers) {
        return smsDAO.removeBlacklist(phoneNumbers);
    }

    @Override
    public List<String> getBlacklist() {
        return smsDAO.getBlacklist();
    }

    @Override
    public Boolean checkBlacklist(String phoneNumber) {
        return smsDAO.checkBlacklist(phoneNumber);
    }

    @Override
    @Transactional
    public Sms_requests updateSmsRecord(Sms_requests sms_requests) {
        return smsDAO.updateSmsRecord(sms_requests);
    }

    @Override
    public List<Object> getAllMessagesWithText(GetMessageTextBody body) throws IOException {
        return smsDAO.getAllMessagesWithText(body);
    }

    @Override
    public List<Object> getAllMessagesWithPhone(GetMessagePhoneBody body) throws IOException {
        return smsDAO.getAllMessagesWithPhone(body);
    }

    public void sendMessage(int message) {
        kafkaTemplate.send(TOPIC, String.valueOf(message));
    }
}
