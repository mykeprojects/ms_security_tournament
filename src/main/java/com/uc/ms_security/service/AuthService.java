package com.uc.ms_security.service;

import com.uc.ms_security.dto.auth.AuthResponseDTO;
import com.uc.ms_security.dto.auth.CredentialLoginResponseDTO;
import com.uc.ms_security.dto.auth.LoginRequestDTO;
import com.uc.ms_security.dto.auth.TwoFactorVerificationDTO;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.service.twofactor.TwoFactorChallengeStarted;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;

    public CredentialLoginResponseDTO login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(this::unauthorized);

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

        return CredentialLoginResponseDTO.authenticated(jwtService.generateToken(user));
    }

    public AuthResponseDTO verifyTwoFactor(TwoFactorVerificationDTO dto) {
        User user = twoFactorService.consumeChallenge(
                dto.getChallengeToken(),
                dto.getCode());
        return jwtService.generateToken(user);
    }

    private ApplicationException unauthorized() {
        return new ApplicationException(
                ErrorCase.UNAUTHORIZED,
                "Credenciales incorrectas");
    }
}
