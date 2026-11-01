package com.uc.ms_security.service.twofactor;

import java.time.Instant;

public record TwoFactorChallengeStarted(
        String challengeToken,
        Instant expiresAt,
        String message) {
}
