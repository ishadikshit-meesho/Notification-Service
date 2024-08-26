package com.notification.server.service;


import com.notification.server.constants.GetMessagePhoneBody;
import com.notification.server.constants.GetMessageTextBody;
import com.notification.server.entity.Sms_requests;

import java.io.IOException;
import java.util.List;

public interface NotificationService {
    Sms_requests createSmsRecord(Sms_requests sms_requests);
    void sendMessage(int message);
    Sms_requests findById(String id);

    String addBlacklist(String[] phoneNumbers);

    String removeBlacklist(String[] phoneNumbers);

    List<String> getBlacklist();

    Boolean checkBlacklist(String phoneNumber);

    Sms_requests updateSmsRecord(Sms_requests sms_requests);

    List<Object> getAllMessagesWithPhone(GetMessagePhoneBody body) throws IOException;

    List<Object> getAllMessagesWithText(GetMessageTextBody body) throws IOException;
}
