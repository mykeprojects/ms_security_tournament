package com.uc.ms_security.service;

import com.uc.ms_security.dto.auth.PasswordRecoveryRequestDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryResponseDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryVerificationDTO;
import com.uc.ms_security.dto.auth.PasswordResetDTO;
import com.uc.ms_security.entity.PasswordResetToken;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.notification.NotificationSender;
import com.uc.ms_security.repository.PasswordResetTokenRepository;
import com.uc.ms_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;
    private static final String RECOVERY_SUBJECT = "Código para recuperar tu contraseña";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationSender notificationSender;
    private final SessionService sessionService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void requestRecovery(PasswordRecoveryRequestDTO dto) {
        userRepository.findByEmail(dto.getEmail().trim())
                .filter(this::isCredentialsUser)
                .ifPresent(this::createAndSendCode);
    }

    @Transactional
    public PasswordRecoveryResponseDTO verifyCode(PasswordRecoveryVerificationDTO dto) {
        User user = findCredentialsUser(dto.getEmail());
        PasswordResetToken recovery = tokenRepository
                .findTopByUserIdAndUsedFalseOrderByIdDesc(user.getId())
                .filter(this::isCodeUsable)
                .orElseThrow(this::invalidCode);

        if (!matches(dto.getCode(), recovery.getCodeHash())) {
            throw invalidCode();
        }

        String resetToken = randomToken();
        recovery.setResetTokenHash(hash(resetToken));
        recovery.setVerifiedAt(LocalDateTime.now());
        tokenRepository.save(recovery);

        return new PasswordRecoveryResponseDTO(
                "Código confirmado correctamente.", resetToken);
    }

    @Transactional
    public void resetPassword(PasswordResetDTO dto) {
        PasswordResetToken recovery = tokenRepository
                .findByResetTokenHashAndUsedFalse(hash(dto.getResetToken()))
                .filter(this::isResetUsable)
                .orElseThrow(this::invalidCode);

        User user = recovery.getUser();
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        recovery.setUsed(true);
        tokenRepository.save(recovery);
        sessionService.revokeAllByUserId(user.getId());
    }

    private void createAndSendCode(User user) {
        tokenRepository.deleteAllByUserId(user.getId());

        String code = String.format("%0" + CODE_LENGTH + "d",
                secureRandom.nextInt((int) Math.pow(10, CODE_LENGTH)));
        PasswordResetToken recovery = new PasswordResetToken();
        recovery.setUser(user);
        recovery.setCodeHash(hash(code));
        recovery.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        recovery.setUsed(false);
        tokenRepository.save(recovery);

        notificationSender.send(
                user.getEmail(),
                RECOVERY_SUBJECT,
                "Tu código para recuperar la contraseña es: " + code
                        + ". Tiene una validez de " + EXPIRATION_MINUTES + " minutos.");
    }

    private User findCredentialsUser(String email) {
        return userRepository.findByEmail(email.trim())
                .filter(this::isCredentialsUser)
                .orElseThrow(this::invalidCode);
    }

    private boolean isCredentialsUser(User user) {
        return user.getAuthProvider() == null
                || "CREDENTIALS".equals(user.getAuthProvider());
    }

    private boolean isCodeUsable(PasswordResetToken token) {
        return token.getVerifiedAt() == null
                && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isResetUsable(PasswordResetToken token) {
        return token.getVerifiedAt() != null
                && token.getExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean matches(String value, String expectedHash) {
        return MessageDigest.isEqual(
                hash(value).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }

    private ApplicationException invalidCode() {
        return new ApplicationException(
                ErrorCase.INVALID_OPERATION,
                "El código no es válido o ha expirado");
    }
}
