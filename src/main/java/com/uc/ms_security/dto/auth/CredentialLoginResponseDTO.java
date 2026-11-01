package com.uc.ms_security.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CredentialLoginResponseDTO(
        boolean requiresTwoFactor,
        String challengeToken,
        Instant expiresAt,
        String accessToken,
        String tokenType,
        String message) {

    public static CredentialLoginResponseDTO authenticated(AuthResponseDTO session) {
        return new CredentialLoginResponseDTO(
                false,
                null,
                session.expiresAt(),
                session.accessToken(),
                session.tokenType(),
                null);
    }

    public static CredentialLoginResponseDTO challenge(
            String challengeToken,
            Instant expiresAt,
            String message) {
        return new CredentialLoginResponseDTO(
                true,
                challengeToken,
                expiresAt,
                null,
                null,
                message);
    }
}
