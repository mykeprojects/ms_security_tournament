package com.uc.ms_security.controller;

import com.uc.ms_security.dto.auth.AuthResponseDTO;
import com.uc.ms_security.dto.auth.CredentialLoginResponseDTO;
import com.uc.ms_security.dto.auth.LoginRequestDTO;
import com.uc.ms_security.dto.auth.ExternalLoginRequestDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryRequestDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryResponseDTO;
import com.uc.ms_security.dto.auth.PasswordRecoveryVerificationDTO;
import com.uc.ms_security.dto.auth.PasswordResetDTO;
import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.service.AuthService;
import com.uc.ms_security.service.PasswordRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/login")
    public CredentialLoginResponseDTO login(@Valid @RequestBody LoginRequestDTO dto) {
        return authService.login(dto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO register(@Valid @RequestBody CreateUserDTO dto) {
        return authService.register(dto);
    }

    @PostMapping("/oauth")
    public AuthResponseDTO loginWithExternalProvider(
            @Valid @RequestBody ExternalLoginRequestDTO dto) {
        return authService.loginWithExternalProvider(dto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(Authentication authentication) {
        authService.logout(authentication);
    }

    @PostMapping("/password-recovery/request")
    public void requestPasswordRecovery(
            @Valid @RequestBody PasswordRecoveryRequestDTO dto) {
        passwordRecoveryService.requestRecovery(dto);
    }

    @PostMapping("/password-recovery/verify")
    public PasswordRecoveryResponseDTO verifyPasswordRecovery(
            @Valid @RequestBody PasswordRecoveryVerificationDTO dto) {
        return passwordRecoveryService.verifyCode(dto);
    }

    @PostMapping("/password-recovery/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody PasswordResetDTO dto) {
        passwordRecoveryService.resetPassword(dto);
    }
}
