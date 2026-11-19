package com.uc.ms_security.controller;

import com.uc.ms_security.dto.security.PermissionValidationRequestDTO;
import com.uc.ms_security.dto.security.PermissionValidationResponseDTO;
import com.uc.ms_security.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/security")
@RequiredArgsConstructor
public class InternalSecurityController {

    private final PermissionService permissionService;

    @PostMapping("/permissions-validation")
    public PermissionValidationResponseDTO validatePermission(
            @Valid @RequestBody PermissionValidationRequestDTO request,
            Authentication authentication) {
        long userId = authenticatedUserId(authentication);
        boolean allowed = permissionService.hasPermission(
                userId,
                request.method(),
                request.url());
        return new PermissionValidationResponseDTO(allowed);
    }

    private long authenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Se requiere autenticación.");
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException("El usuario autenticado no es válido.");
        }
    }
}
