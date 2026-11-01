package com.uc.ms_security.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class BackendNotificationSender implements NotificationSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendNotificationSender.class);

    private static final class NotificationResponseType
            extends ParameterizedTypeReference<Map<String, Object>> {
    }

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new NotificationResponseType();

    private final RestClient client;
    private final String baseUrl;

    public BackendNotificationSender(
            @Value("${security.notification.base-url:http://127.0.0.1:5000}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(20000);
        this.baseUrl = baseUrl;
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public void send(String recipient, String subject, String content) {
        dispatch(recipient, subject, content);
    }

    @Override
    public NotificationDispatch dispatch(String recipient, String subject, String content) {
        try {
            Map<String, Object> response = client.post()
                    .uri("/notificar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "para", recipient,
                            "asunto", subject,
                            "contenido", content))
                    .retrieve()
                    .body(RESPONSE_TYPE);
            boolean deliveredByEmail = response == null
                    || !"local".equals(String.valueOf(response.get("modo")));
            return new NotificationDispatch(deliveredByEmail);
        } catch (RestClientException exception) {
            LOGGER.warn(
                    "API de notificaciones no disponible en {}/notificar. Se usará modo local.",
                    baseUrl);
            return new NotificationDispatch(false);
        }
    }
}
