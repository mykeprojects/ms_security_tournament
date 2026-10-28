package com.uc.ms_security.config;

import com.uc.ms_security.authorization.DynamicAuthorizationManager;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SecurityConfigCorsTest {

    @Test
    void allowsLocalFrontendOriginsWithoutAllowingArbitraryOrigins() {
        CorsConfiguration configuration = new SecurityConfig(
                mock(DynamicAuthorizationManager.class))
                .corsConfigurationSource(
                        "http://localhost:[*],http://127.0.0.1:[*]")
                .getCorsConfiguration(new MockHttpServletRequest(
                        "GET", "/api/users"));

        assertEquals(
                "http://localhost:5173",
                configuration.checkOrigin("http://localhost:5173"));
        assertNull(configuration.checkOrigin("https://example.com"));
    }
}
