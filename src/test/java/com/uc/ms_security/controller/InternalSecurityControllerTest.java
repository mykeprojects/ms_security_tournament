package com.uc.ms_security.controller;

import com.uc.ms_security.dto.security.PermissionValidationRequestDTO;
import com.uc.ms_security.dto.security.PermissionValidationResponseDTO;
import com.uc.ms_security.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalSecurityControllerTest {

    private PermissionService permissionService;
    private InternalSecurityController controller;

    @BeforeEach
    void setUp() {
        permissionService = mock(PermissionService.class);
        controller = new InternalSecurityController(permissionService);
    }

    @Test
    void checksRequestedMethodAndPathForTheAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("42");
        when(permissionService.hasPermission(42L, "GET", "/sports"))
                .thenReturn(true);

        PermissionValidationResponseDTO response = controller.validatePermission(
                new PermissionValidationRequestDTO("GET", "/sports"),
                authentication);

        assertEquals(true, response.allowed());
        verify(permissionService).hasPermission(42L, "GET", "/sports");
    }
}
