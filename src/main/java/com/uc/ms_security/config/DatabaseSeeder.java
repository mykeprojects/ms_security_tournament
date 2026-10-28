package com.uc.ms_security.config;

import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.entity.UserRole;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private static final PermissionSeed[] ADMIN_PERMISSIONS = {
            new PermissionSeed("GET", "/api/users", "users"),
            new PermissionSeed("POST", "/api/users", "users"),
            new PermissionSeed("GET", "/api/users/{id}", "users"),
            new PermissionSeed("GET", "/api/users/{id}/detail", "users"),
            new PermissionSeed("PUT", "/api/users/{id}", "users"),
            new PermissionSeed("DELETE", "/api/users/{id}", "users"),
            new PermissionSeed("GET", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("POST", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("PUT", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("DELETE", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("GET", "/api/roles", "roles"),
            new PermissionSeed("POST", "/api/roles", "roles"),
            new PermissionSeed("GET", "/api/roles/{id}", "roles"),
            new PermissionSeed("PUT", "/api/roles/{id}", "roles"),
            new PermissionSeed("DELETE", "/api/roles/{id}", "roles"),
            new PermissionSeed("GET", "/api/permissions", "permissions"),
            new PermissionSeed("POST", "/api/permissions", "permissions"),
            new PermissionSeed("GET", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("PUT", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("DELETE", "/api/permissions/{id}", "permissions")
    };

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDatabase() {
        return arguments -> {
            Role adminRole = findOrCreateRole("ADMIN", "Administrador");
            Role consultantRole = findOrCreateRole(
                    "CONSULTOR",
                    "Consulta de usuarios");
            User adminUser = findOrCreateUser();

            assignRole(adminUser, adminRole);
            for (PermissionSeed seed : ADMIN_PERMISSIONS) {
                assignPermission(adminRole, findOrCreatePermission(seed));
            }
            assignPermission(
                    consultantRole,
                    findOrCreatePermission(
                            new PermissionSeed("GET", "/api/users", "users")));
            assignPermission(
                    consultantRole,
                    findOrCreatePermission(
                            new PermissionSeed("GET", "/api/users/{id}", "users")));
        };
    }

    private Role findOrCreateRole(String name, String description) {
        return roleRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            return roleRepository.save(role);
        });
    }

    private User findOrCreateUser() {
        return userRepository.findByEmail("admin@uc.edu.co").orElseGet(() -> {
            User user = new User();
            user.setName("Administrador");
            user.setEmail("admin@uc.edu.co");
            user.setPassword(passwordEncoder.encode("Admin123!"));
            return userRepository.save(user);
        });
    }

    private Permission findOrCreatePermission(PermissionSeed seed) {
        return permissionRepository.findByUrlAndMethod(
                seed.url(), seed.method()).orElseGet(() -> {
            Permission permission = new Permission();
            permission.setUrl(seed.url());
            permission.setMethod(seed.method());
            permission.setModel(seed.model());
            return permissionRepository.save(permission);
        });
    }

    private void assignRole(User user, Role role) {
        if (!userRoleRepository.existsByUserIdAndRoleId(
                user.getId(), role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    private void assignPermission(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(
                role.getId(), permission.getId())) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }

    private record PermissionSeed(String method, String url, String model) {
    }
}
