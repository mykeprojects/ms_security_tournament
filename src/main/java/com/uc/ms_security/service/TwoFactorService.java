package com.uc.ms_security.service;

import com.uc.ms_security.dto.auth.TwoFactorStatusDTO;
import com.uc.ms_security.entity.TwoFactorChallenge;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.notification.NotificationDispatch;
import com.uc.ms_security.notification.NotificationSender;
import com.uc.ms_security.repository.TwoFactorChallengeRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.service.twofactor.CredentialsAccountPolicy;
import com.uc.ms_security.service.twofactor.TwoFactorChallengeStarted;
import com.uc.ms_security.service.twofactor.VerificationCodeGenerator;
import com.uc.ms_security.service.twofactor.VerificationDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private static final int EXPIRATION_MINUTES = 5;
    private static final String CODE_SUBJECT = "Código de verificación de dos factores";

    private final TwoFactorChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final NotificationSender notificationSender;
    private final CredentialsAccountPolicy credentialsAccountPolicy;
    private final VerificationCodeGenerator codeGenerator;
    private final VerificationDigest verificationDigest;

    public boolean requiresSecondFactor(User user) {
        return credentialsAccountPolicy.canUseTwoFactor(user) && isEnabled(user);
    }

    public boolean isEnabled(User user) {
        return Boolean.TRUE.equals(user.getTwoFactorEnabled());
    }

    @Transactional(readOnly = true)
    public TwoFactorStatusDTO getStatus(Long userId) {
        User user = findUser(userId);
        boolean available = credentialsAccountPolicy.canUseTwoFactor(user);
        return new TwoFactorStatusDTO(available, available && isEnabled(user));
    }

    @Transactional
    public TwoFactorStatusDTO updatePreference(Long userId, boolean enabled) {
        User user = findUser(userId);
        credentialsAccountPolicy.requireTwoFactorEligible(user);
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
        if (!enabled) {
            challengeRepository.deleteAllByUserIdAndUsedFalse(userId);
        }
        return new TwoFactorStatusDTO(true, enabled);
    }

    @Transactional
    public TwoFactorChallengeStarted startLoginChallenge(User user) {
        if (!requiresSecondFactor(user)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "El usuario no tiene autenticación de dos factores habilitada");
        }

        challengeRepository.deleteAllByUserIdAndUsedFalse(user.getId());

        String code = codeGenerator.numericCode();
        String challengeToken = codeGenerator.opaqueToken();

        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setUser(user);
        challenge.setCodeHash(verificationDigest.hash(code));
        challenge.setChallengeTokenHash(verificationDigest.hash(challengeToken));
        challenge.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(EXPIRATION_MINUTES));
        challenge.setUsed(false);
        challengeRepository.save(challenge);

        NotificationDispatch dispatch;
        try {
            dispatch = notificationSender.dispatch(
                    user.getEmail(),
                    CODE_SUBJECT,
                    code);
        } catch (RuntimeException exception) {
            challengeRepository.delete(challenge);
            throw exception;
        }

        String message = dispatch.deliveredByEmail()
                ? "Se envió un código de verificación a tu correo"
                : "Gmail no está configurado. Código temporal: " + code;

        return new TwoFactorChallengeStarted(
                challengeToken,
                challenge.getExpiresAt().toInstant(ZoneOffset.UTC),
                message);
    }

    @Transactional
    public User consumeChallenge(String challengeToken, String code) {
        TwoFactorChallenge challenge = challengeRepository
                .findByChallengeTokenHash(verificationDigest.hash(challengeToken))
                .filter(this::isUsable)
                .orElseThrow(this::invalidChallenge);

        if (!verificationDigest.matches(code, challenge.getCodeHash())) {
            throw invalidChallenge();
        }

        challenge.setUsed(true);
        challengeRepository.save(challenge);
        return challenge.getUser();
    }

    private boolean isUsable(TwoFactorChallenge challenge) {
        return !challenge.isUsed()
                && challenge.getExpiresAt().isAfter(LocalDateTime.now(ZoneOffset.UTC));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Usuario no encontrado con id: " + userId));
    }

    private ApplicationException invalidChallenge() {
        return new ApplicationException(
                ErrorCase.INVALID_OPERATION,
                "El código no es válido o ha expirado");
    }
}
