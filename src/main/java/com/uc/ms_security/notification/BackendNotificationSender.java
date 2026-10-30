package com.uc.ms_security.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class BackendNotificationSender implements NotificationSender {

    private final RestClient client;

    public BackendNotificationSender(
            @Value("${security.notification.base-url:http://localhost:5000}") String baseUrl) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public void send(String recipient, String subject, String content) {
        try {
            client.post()
                    .uri("/notificar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "para", recipient,
                            "asunto", subject,
                            "contenido", content))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new NotificationDeliveryException(
                    "No fue posible enviar el correo de recuperación", exception);
        }
    }
}
