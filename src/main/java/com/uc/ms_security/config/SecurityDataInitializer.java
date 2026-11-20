package com.uc.ms_security.config;

import com.uc.ms_security.service.RolePermissionSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityDataInitializer implements ApplicationRunner {

    private final RolePermissionSetService rolePermissionSetService;

    @Override
    public void run(ApplicationArguments args) {
        rolePermissionSetService.ensureAdminHasAllPermissions();
    }
}
