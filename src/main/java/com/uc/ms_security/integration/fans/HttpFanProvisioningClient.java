package com.uc.ms_security.integration.fans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * HTTP adapter for the sports microservice internal fan endpoint.
 *
 * <p>The call is part of the registration saga, so failures are translated
 * into {@link FanProvisioningException} and the orchestrator compensates the
 * account created in the security database.</p>
 */
@Component
public class HttpFanProvisioningClient implements FanProvisioningClient {

    private static final Logger logger =
            LoggerFactory.getLogger(HttpFanProvisioningClient.class);
    private static final String API_KEY_HEADER = "x-internal-api-key";

    private final RestClient client;
    private final String apiKey;

    public HttpFanProvisioningClient(
            @Value("${security.sports.base-url:http://localhost:3000}") String baseUrl,
            @Value("${security.sports.internal-api-key:}") String apiKey) {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        this.apiKey = apiKey;
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public void provision(FanProvisioningRequest request) {
        try {
            client.post()
                    .uri("/internal/fans")
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new FanProvisioningException(
                    "No fue posible crear el perfil de aficionado en el sistema deportivo",
                    exception);
        }
    }

    @Override
    public void delete(Long externalUserId) {
        try {
            client.delete()
                    .uri("/internal/fans/{externalUserId}", externalUserId)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return;
            }
            logger.warn(
                    "No se pudo eliminar el aficionado {} durante la compensación: {}",
                    externalUserId,
                    exception.getStatusCode());
            throw new FanProvisioningException(
                    "No fue posible eliminar el perfil de aficionado durante la compensación",
                    exception);
        } catch (RestClientException exception) {
            throw new FanProvisioningException(
                    "No fue posible eliminar el perfil de aficionado durante la compensación",
                    exception);
        }
    }
}
