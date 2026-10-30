package com.uc.ms_security.service.identity;

public record ExternalIdentity(
        String provider,
        String subject,
        String email,
        String name) {
}
