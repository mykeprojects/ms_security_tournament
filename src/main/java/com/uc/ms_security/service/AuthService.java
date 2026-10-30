package com.uc.ms_security.service;

import com.uc.ms_security.dto.auth.AuthResponseDTO;
import com.uc.ms_security.dto.auth.CredentialLoginResponseDTO;
import com.uc.ms_security.dto.auth.LoginRequestDTO;
import com.uc.ms_security.dto.auth.ExternalLoginRequestDTO;
import com.uc.ms_security.dto.auth.TwoFactorVerificationDTO;
import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.service.identity.ExternalIdentity;
import com.uc.ms_security.service.identity.ExternalIdentityProvider;
import com.uc.ms_security.service.twofactor.TwoFactorChallengeStarted;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final SessionService sessionService;
    private final TwoFactorService twoFactorService;
    private final List<ExternalIdentityProvider> externalIdentityProviders;

    public CredentialLoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(this::unauthorized);

        if (user.getAuthProvider() != null
                && !"CREDENTIALS".equals(user.getAuthProvider())) {
            throw unauthorized();
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw unauthorized();
        }

        if (twoFactorService.requiresSecondFactor(user)) {
            TwoFactorChallengeStarted challenge = twoFactorService.startLoginChallenge(user);
            return CredentialLoginResponseDTO.challenge(
                    challenge.challengeToken(),
                    challenge.expiresAt(),
                    challenge.message());
        }

        return CredentialLoginResponseDTO.authenticated(createSession(user));
    }

    public AuthResponseDTO verifyTwoFactor(TwoFactorVerificationDTO dto) {
        User user = twoFactorService.consumeChallenge(
                dto.getChallengeToken(),
                dto.getCode());
        return createSession(user);
    }

    public UserResponseDTO register(CreateUserDTO dto) {
        return userService.create(dto);
    }

    public AuthResponseDTO loginWithExternalProvider(ExternalLoginRequestDTO dto) {
        ExternalIdentityProvider provider = externalIdentityProviders.stream()
                .filter(candidate -> candidate.supports(dto.getProvider()))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.INVALID_OPERATION,
                        "El proveedor externo no está disponible"));

        ExternalIdentity identity = provider.verify(
                dto.getIdToken(),
                dto.getProvider());
        User user = userRepository.findByEmail(identity.email())
                .map(existingUser -> validateExternalAccount(existingUser, identity))
                .orElseGet(() -> userService.createExternal(
                        identity.name(),
                        identity.email(),
                        identity.provider(),
                        identity.subject()));

        return createSession(user);
    }

    public void logout(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            sessionService.revokeByToken(jwtAuthentication.getToken().getTokenValue());
        }
    }

    private AuthResponseDTO createSession(User user) {
        AuthResponseDTO response = jwtService.generateToken(user);
        sessionService.createForAuthentication(
                user,
                response.accessToken(),
                response.expiresAt());
        return response;
    }

    private User validateExternalAccount(User user, ExternalIdentity identity) {
        if (!identity.provider().equals(user.getAuthProvider())
                || !identity.subject().equals(user.getExternalSubject())) {
            throw new ApplicationException(
                    ErrorCase.CONFLICT,
                    "El correo ya está asociado a otro mecanismo de autenticación");
        }
        return user;
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(
                ErrorCase.UNAUTHORIZED,
                "Credenciales incorrectas");
    }
}
