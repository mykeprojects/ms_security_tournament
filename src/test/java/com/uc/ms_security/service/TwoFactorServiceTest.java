package com.uc.ms_security.service;

import com.uc.ms_security.entity.TwoFactorChallenge;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.notification.NotificationDispatch;
import com.uc.ms_security.notification.NotificationSender;
import com.uc.ms_security.repository.TwoFactorChallengeRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.service.twofactor.CredentialsAccountPolicy;
import com.uc.ms_security.service.twofactor.TwoFactorChallengeStarted;
import com.uc.ms_security.service.twofactor.VerificationCodeGenerator;
import com.uc.ms_security.service.twofactor.VerificationDigest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoFactorServiceTest {

    @Mock
    private TwoFactorChallengeRepository challengeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationSender notificationSender;
    @Mock
    private VerificationCodeGenerator codeGenerator;

    private TwoFactorService twoFactorService;
    private VerificationDigest digest;

    @BeforeEach
    void setUp() {
        digest = new VerificationDigest();
        twoFactorService = new TwoFactorService(
                challengeRepository,
                userRepository,
                notificationSender,
                new CredentialsAccountPolicy(),
                codeGenerator,
                digest);
    }

    @Test
    void requiresSecondFactorOnlyForCredentialsUsersWithFlagEnabled() {
        User credentialsUser = credentialsUser(true);
        User oauthUser = oauthUser(true);

        assertTrue(twoFactorService.requiresSecondFactor(credentialsUser));
        assertFalse(twoFactorService.requiresSecondFactor(oauthUser));
        assertFalse(twoFactorService.requiresSecondFactor(credentialsUser(false)));
        assertFalse(twoFactorService.requiresSecondFactor(seededAdmin(true)));
    }

    @Test
    void seededAdminCannotEnableTwoFactor() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(seededAdmin(false)));

        assertThrows(
                ApplicationException.class,
                () -> twoFactorService.updatePreference(1L, true));
    }

    @Test
    void startLoginChallengeSendsCodeThroughNotificationApi() {
        User user = credentialsUser(true);
        when(codeGenerator.numericCode()).thenReturn("123456");
        when(codeGenerator.opaqueToken()).thenReturn("challenge-token");
        when(challengeRepository.save(any(TwoFactorChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationSender.dispatch(any(), any(), any()))
                .thenReturn(new NotificationDispatch(true));

        TwoFactorChallengeStarted started = twoFactorService.startLoginChallenge(user);

        assertEquals("challenge-token", started.challengeToken());
        assertEquals("Se envió un código de verificación a tu correo", started.message());
        verify(notificationSender).dispatch(
                eq("ana@uc.edu.co"),
                eq("Código de verificación de dos factores"),
                eq("123456"));
    }

    @Test
    void consumeChallengeRejectsExpiredOrUsedCodes() {
        User user = credentialsUser(true);
        TwoFactorChallenge expired = challenge(user, "654321", true, false);
        when(challengeRepository.findByChallengeTokenHash(digest.hash("token")))
                .thenReturn(Optional.of(expired));

        assertThrows(
                ApplicationException.class,
                () -> twoFactorService.consumeChallenge("token", "654321"));
        verify(challengeRepository, never()).save(any());
    }

    @Test
    void consumeChallengeMarksCodeAsUsed() {
        User user = credentialsUser(true);
        TwoFactorChallenge challenge = challenge(user, "654321", false, false);
        when(challengeRepository.findByChallengeTokenHash(digest.hash("token")))
                .thenReturn(Optional.of(challenge));
        when(challengeRepository.save(any(TwoFactorChallenge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User authenticated = twoFactorService.consumeChallenge("token", "654321");

        assertEquals(user, authenticated);
        ArgumentCaptor<TwoFactorChallenge> captor =
                ArgumentCaptor.forClass(TwoFactorChallenge.class);
        verify(challengeRepository).save(captor.capture());
        assertTrue(captor.getValue().isUsed());
    }

    @Test
    void updatePreferenceRejectsOauthAccounts() {
        when(userRepository.findById(8L)).thenReturn(Optional.of(oauthUser(false)));

        assertThrows(
                ApplicationException.class,
                () -> twoFactorService.updatePreference(8L, true));
    }

    private User credentialsUser(boolean enabled) {
        User user = new User();
        user.setId(3L);
        user.setEmail("ana@uc.edu.co");
        user.setAuthProvider("CREDENTIALS");
        user.setTwoFactorEnabled(enabled);
        return user;
    }

    private User seededAdmin(boolean enabled) {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@uc.edu.co");
        user.setAuthProvider("CREDENTIALS");
        user.setTwoFactorEnabled(enabled);
        return user;
    }

    private User oauthUser(boolean enabled) {
        User user = new User();
        user.setId(8L);
        user.setEmail("oauth@uc.edu.co");
        user.setAuthProvider("GOOGLE");
        user.setTwoFactorEnabled(enabled);
        return user;
    }

    private TwoFactorChallenge challenge(
            User user,
            String code,
            boolean expired,
            boolean used) {
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setUser(user);
        challenge.setCodeHash(digest.hash(code));
        challenge.setChallengeTokenHash(digest.hash("token"));
        challenge.setUsed(used);
        challenge.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(expired ? -1 : 5));
        return challenge;
    }
}
