package com.uc.ms_security.integration.fans;

/**
 * Minimal identity data the sports microservice needs to create a fan.
 * Authentication data (password, tokens, provider) is never shared.
 */
public record FanProvisioningRequest(
        Long externalUserId,
        String email,
        String fullName) {
}
