package com.uc.ms_security.service.registration;

import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.service.UserRoleService;
import com.uc.ms_security.service.UserService;
import com.uc.ms_security.service.identity.ExternalIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the security-database part of a new account: user plus default role.
 * It never talks to the sports microservice.
 */
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    public static final String DEFAULT_ROLE_NAME = "AFICIONADO";

    private final UserService userService;
    private final UserRoleService userRoleService;

    @Transactional
    public UserResponseDTO createCredentialUser(CreateUserDTO dto) {
        UserResponseDTO user = userService.create(dto);
        userRoleService.assignIfMissing(user.getId(), DEFAULT_ROLE_NAME);
        return user;
    }

    @Transactional
    public User createExternalUser(ExternalIdentity identity) {
        User user = userService.createExternal(
                identity.name(),
                identity.email(),
                identity.provider(),
                identity.subject());
        userRoleService.assignIfMissing(user.getId(), DEFAULT_ROLE_NAME);
        return user;
    }

    @Transactional
    public void compensate(Long userId) {
        userService.delete(userId);
    }
}
