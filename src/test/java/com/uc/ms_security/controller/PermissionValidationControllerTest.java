package com.uc.ms_security.controller;

import com.uc.ms_security.dto.PermissionValidationRequestDTO;
import com.uc.ms_security.dto.PermissionValidationResponseDTO;
import com.uc.ms_security.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionValidationControllerTest {

    private PermissionService permissionService;
    private PermissionValidationController controller;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        controller = new PermissionValidationController(permissionService);
    }

    @Test
    void returnsPermissionDecisionUsingUserIdFromJwtSubject() {
        PermissionValidationRequestDTO request = request("/cinema/movies", "post");
        when(permissionService.hasPermission(
                42L, "post", "/cinema/movies")).thenReturn(true);

        PermissionValidationResponseDTO response = controller.validate(
                request,
                authenticationForUser(42L));

        assertTrue(response.allowed());
        verify(permissionService).hasPermission(
                42L, "post", "/cinema/movies");
    }

    @Test
    void returnsFalseWhenPermissionServiceDeniesAccess() {
        PermissionValidationRequestDTO request = request(
                "/cinema/movies/15", "GET");
        when(permissionService.hasPermission(
                42L, "GET", "/cinema/movies/15")).thenReturn(false);

        PermissionValidationResponseDTO response = controller.validate(
                request,
                authenticationForUser(42L));

        assertFalse(response.allowed());
    }

    private PermissionValidationRequestDTO request(String url, String method) {
        PermissionValidationRequestDTO request =
                new PermissionValidationRequestDTO();
        request.setUrl(url);
        request.setMethod(method);
        return request;
    }

    private JwtAuthenticationToken authenticationForUser(long userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("sub", Long.toString(userId))
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
