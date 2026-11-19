package com.uc.ms_security.dto.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PermissionValidationRequestDTO(
        @NotBlank
        @Size(max = 10)
        String method,
        @NotBlank
        @Size(max = 255)
        String url) {
}
