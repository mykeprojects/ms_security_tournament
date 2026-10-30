package com.uc.ms_security.controller;

import com.uc.ms_security.dto.auth.AuthResponseDTO;
import com.uc.ms_security.dto.auth.TwoFactorPreferenceDTO;
import com.uc.ms_security.dto.auth.TwoFactorStatusDTO;
import com.uc.ms_security.dto.auth.TwoFactorVerificationDTO;
import com.uc.ms_security.service.AuthService;
import com.uc.ms_security.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final AuthService authService;
    private final TwoFactorService twoFactorService;

    @GetMapping
    public TwoFactorStatusDTO status(Authentication authentication) {
        return twoFactorService.getStatus(currentUserId(authentication));
    }

    @PutMapping
    public TwoFactorStatusDTO updatePreference(
            Authentication authentication,
            @Valid @RequestBody TwoFactorPreferenceDTO dto) {
        return twoFactorService.updatePreference(
                currentUserId(authentication),
                Boolean.TRUE.equals(dto.getEnabled()));
    }

    @PostMapping("/verify")
    public AuthResponseDTO verify(@Valid @RequestBody TwoFactorVerificationDTO dto) {
        return authService.verifyTwoFactor(dto);
    }

    private Long currentUserId(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
