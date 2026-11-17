package com.uc.ms_security.controller;

import com.uc.ms_security.dto.permission.PermissionValidationRequest;
import com.uc.ms_security.dto.permission.PermissionValidationResponse;
import com.uc.ms_security.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/internal/security")
@RequiredArgsConstructor
public class PermissionValidationController {
    private final PermissionService permissionService;

    @PostMapping("/permissions-validation")
    public PermissionValidationResponse validate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PermissionValidationRequest request) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
        }
        final long userId;
        try {
            userId = Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido o expirado");
        }
        boolean allowed = permissionService.hasPermission(userId, request.getMethod(), request.getUrl());
        return new PermissionValidationResponse(allowed);
    }
}
