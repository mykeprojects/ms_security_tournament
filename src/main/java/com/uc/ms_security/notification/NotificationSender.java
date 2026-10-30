package com.uc.ms_security.notification;

public interface NotificationSender {

    void send(String recipient, String subject, String content);

    default NotificationDispatch dispatch(String recipient, String subject, String content) {
        send(recipient, subject, content);
        return new NotificationDispatch(true);
    }
}
