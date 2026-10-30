package com.uc.ms_security.notification;

public interface NotificationSender {

    void send(String recipient, String subject, String content);
}
