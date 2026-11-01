package com.uc.ms_security.dto.auth;

public record TwoFactorStatusDTO(
        boolean available,
        boolean enabled) {
}
