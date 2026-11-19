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
import com.uc.ms_security.service.RolePermissionSetService;
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
            new PermissionSeed("GET", "/api/user-roles", "user-roles"),
            new PermissionSeed("POST", "/api/user-roles", "user-roles"),
            new PermissionSeed("DELETE", "/api/user-roles/{id}/{id}", "user-roles"),
            new PermissionSeed("GET", "/api/roles", "roles"),
            new PermissionSeed("POST", "/api/roles", "roles"),
            new PermissionSeed("GET", "/api/roles/{id}", "roles"),
            new PermissionSeed("PUT", "/api/roles/{id}", "roles"),
            new PermissionSeed("DELETE", "/api/roles/{id}", "roles"),
            new PermissionSeed("GET", "/api/role-permissions", "role-permissions"),
            new PermissionSeed("POST", "/api/role-permissions", "role-permissions"),
            new PermissionSeed(
                    "DELETE",
                    "/api/role-permissions/{id}",
                    "role-permissions"),
            new PermissionSeed("GET", "/api/permissions", "permissions"),
            new PermissionSeed("POST", "/api/permissions", "permissions"),
            new PermissionSeed("GET", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("PUT", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("DELETE", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("GET", "/sports", "sports"),
            new PermissionSeed("GET", "/sports/{id}", "sports"),
            new PermissionSeed("POST", "/sports", "sports"),
            new PermissionSeed("PATCH", "/sports/{id}", "sports"),
            new PermissionSeed("DELETE", "/sports/{id}", "sports"),
            new PermissionSeed("GET", "/teams", "teams"),
            new PermissionSeed("GET", "/teams/{id}", "teams"),
            new PermissionSeed("POST", "/teams", "teams"),
            new PermissionSeed("PATCH", "/teams/{id}", "teams"),
            new PermissionSeed("DELETE", "/teams/{id}", "teams"),
            new PermissionSeed("GET", "/teams/{id}/sports", "teams"),
            new PermissionSeed("POST", "/teams/{id}/sports", "teams"),
            new PermissionSeed("DELETE", "/teams/{id}/sports/{id}", "teams"),
            new PermissionSeed("GET", "/teams/{id}/roster", "contracts"),
            new PermissionSeed("POST", "/teams/{id}/players", "contracts"),
            new PermissionSeed("PATCH", "/contracts/{id}", "contracts"),
            new PermissionSeed("POST", "/contracts/{id}/release", "contracts"),
            new PermissionSeed("GET", "/players", "players"),
            new PermissionSeed("GET", "/players/{id}", "players"),
            new PermissionSeed("GET", "/players/{id}/profile", "players"),
            new PermissionSeed("POST", "/players", "players"),
            new PermissionSeed("PATCH", "/players/{id}", "players"),
            new PermissionSeed("DELETE", "/players/{id}", "players"),
            new PermissionSeed("POST", "/players/{id}/skills", "players"),
            new PermissionSeed("DELETE", "/players/{id}/skills/{id}", "players"),
            new PermissionSeed("GET", "/skills", "skills"),
            new PermissionSeed("GET", "/skills/{id}", "skills"),
            new PermissionSeed("POST", "/skills", "skills"),
            new PermissionSeed("PATCH", "/skills/{id}", "skills"),
            new PermissionSeed("DELETE", "/skills/{id}", "skills"),
            new PermissionSeed("GET", "/tournaments", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}/development", "tournaments"),
            new PermissionSeed("POST", "/tournaments", "tournaments"),
            new PermissionSeed("PATCH", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("DELETE", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}/registrations", "tournament-registrations"),
            new PermissionSeed("POST", "/tournaments/{id}/registrations", "tournament-registrations"),
            new PermissionSeed(
                    "DELETE",
                    "/tournaments/{id}/registrations/{id}",
                    "tournament-registrations"),
            new PermissionSeed("GET", "/tournaments/{id}/phases", "tournament-phases"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}", "tournament-phases"),
            new PermissionSeed("POST", "/tournaments/{id}/phases", "tournament-phases"),
            new PermissionSeed("PATCH", "/tournaments/{id}/phases/{id}", "tournament-phases"),
            new PermissionSeed("DELETE", "/tournaments/{id}/phases/{id}", "tournament-phases"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}/groups", "tournament-groups"),
            new PermissionSeed("POST", "/tournaments/{id}/phases/{id}/groups", "tournament-groups"),
            new PermissionSeed("PATCH", "/tournaments/{id}/phases/{id}/groups/{id}", "tournament-groups"),
            new PermissionSeed("DELETE", "/tournaments/{id}/phases/{id}/groups/{id}", "tournament-groups"),
            new PermissionSeed("POST", "/tournaments/{id}/phases/{id}/groups/{id}/teams", "tournament-groups"),
            new PermissionSeed("DELETE", "/tournaments/{id}/phases/{id}/groups/{id}/teams/{id}", "tournament-groups"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}/brackets", "tournament-brackets"),
            new PermissionSeed("POST", "/tournaments/{id}/phases/{id}/brackets/generate", "tournament-brackets"),
            new PermissionSeed("POST", "/tournaments/{id}/phases/{id}/brackets", "tournament-brackets"),
            new PermissionSeed("PATCH", "/tournaments/{id}/phases/{id}/brackets/{id}/winner", "tournament-brackets"),
            new PermissionSeed("DELETE", "/tournaments/{id}/phases/{id}/brackets/{id}", "tournament-brackets"),
            new PermissionSeed("GET", "/tournaments/{id}/matches", "matches"),
            new PermissionSeed("POST", "/tournaments/{id}/matches", "matches"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}/matches", "matches"),
            new PermissionSeed("POST", "/tournaments/{id}/phases/{id}/matches", "matches"),
            new PermissionSeed("GET", "/matches", "matches"),
            new PermissionSeed("GET", "/matches/available", "matches"),
            new PermissionSeed("GET", "/matches/upcoming", "matches"),
            new PermissionSeed("GET", "/matches/{id}", "matches"),
            new PermissionSeed("POST", "/matches", "matches"),
            new PermissionSeed("POST", "/matches/reminders/notify", "matches"),
            new PermissionSeed("PATCH", "/matches/{id}", "matches"),
            new PermissionSeed("PATCH", "/matches/{id}/result", "matches"),
            new PermissionSeed("DELETE", "/matches/{id}", "matches"),
            new PermissionSeed("GET", "/matches/{id}/bets", "bets"),
            new PermissionSeed("POST", "/matches/{id}/settle", "bets"),
            new PermissionSeed("GET", "/bets/me", "bets"),
            new PermissionSeed("POST", "/bets", "bets"),
            new PermissionSeed("PATCH", "/bets/{id}/cancel", "bets"),
            new PermissionSeed("GET", "/fans/me", "fans"),
            new PermissionSeed("GET", "/fans/me/payment-methods", "fans"),
            new PermissionSeed("POST", "/fans/me/payment-methods", "fans"),
            new PermissionSeed("PATCH", "/fans/me/payment-methods/{id}", "fans"),
            new PermissionSeed("DELETE", "/fans/me/payment-methods/{id}", "fans"),
            new PermissionSeed("GET", "/payment-method", "payment-method"),
            new PermissionSeed("GET", "/payment-method/{id}", "payment-method"),
            new PermissionSeed("POST", "/payment-method", "payment-method"),
            new PermissionSeed("PATCH", "/payment-method/{id}", "payment-method"),
            new PermissionSeed("DELETE", "/payment-method/{id}", "payment-method"),
            new PermissionSeed("GET", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed("POST", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed(
                    "PATCH",
                    "/matches/{id}/annotations/{id}",
                    "annotations"),
            new PermissionSeed(
                    "DELETE",
                    "/matches/{id}/annotations/{id}",
                    "annotations")
    };

    private static final PermissionSeed[] FAN_PERMISSIONS = {
            new PermissionSeed("GET", "/matches", "matches"),
            new PermissionSeed("GET", "/matches/available", "matches"),
            new PermissionSeed("GET", "/matches/upcoming", "matches"),
            new PermissionSeed("GET", "/matches/{id}", "matches"),
            new PermissionSeed("GET", "/tournaments/{id}/matches", "matches"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}/matches", "matches"),
            new PermissionSeed("GET", "/tournaments/{id}/phases", "tournament-phases"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}", "tournament-phases"),
            new PermissionSeed("GET", "/teams", "teams"),
            new PermissionSeed("GET", "/teams/{id}", "teams"),
            new PermissionSeed("GET", "/tournaments", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}/development", "tournaments"),
            new PermissionSeed("GET", "/fans/me", "fans"),
            new PermissionSeed("GET", "/fans/me/payment-methods", "fans"),
            new PermissionSeed("POST", "/fans/me/payment-methods", "fans"),
            new PermissionSeed("PATCH", "/fans/me/payment-methods/{id}", "fans"),
            new PermissionSeed("DELETE", "/fans/me/payment-methods/{id}", "fans"),
            new PermissionSeed("GET", "/fans/me/followed-teams", "fans"),
            new PermissionSeed("POST", "/fans/me/followed-teams", "fans"),
            new PermissionSeed("DELETE", "/fans/me/followed-teams/{id}", "fans"),
            new PermissionSeed("GET", "/bets/me", "bets"),
            new PermissionSeed("POST", "/bets", "bets"),
            new PermissionSeed("PATCH", "/bets/{id}/cancel", "bets")
    };

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolePermissionSetService rolePermissionSetService;

    @Bean
    CommandLineRunner seedDatabase() {
        return arguments -> {
            Role adminRole = findOrCreateRole("ADMIN", "Administrador");
            Role consultantRole = findOrCreateRole(
                    "CONSULTOR",
                    "Consulta de usuarios");
            Role fanRole = findOrCreateRole(
                    "AFICIONADO",
                    "Aficionado");
            User adminUser = findOrCreateUser();

            assignRole(adminUser, adminRole);
            for (PermissionSeed seed : ADMIN_PERMISSIONS) {
                assignPermission(adminRole, findOrCreatePermission(seed));
            }
            for (PermissionSeed seed : FAN_PERMISSIONS) {
                assignPermission(fanRole, findOrCreatePermission(seed));
            }
            assignPermission(
                    consultantRole,
                    findOrCreatePermission(
                            new PermissionSeed("GET", "/api/users", "users")));
            assignPermission(
                    consultantRole,
                    findOrCreatePermission(
                            new PermissionSeed("GET", "/api/users/{id}", "users")));
            rolePermissionSetService.ensureAdminHasAllPermissions();
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
        User user = userRepository.findByEmail("admin@uc.edu.co").orElseGet(() -> {
            User created = new User();
            created.setName("Administrador");
            created.setEmail("admin@uc.edu.co");
            created.setPassword(passwordEncoder.encode("Admin123!"));
            created.setTwoFactorEnabled(false);
            return userRepository.save(created);
        });
        if (!Boolean.FALSE.equals(user.getTwoFactorEnabled())) {
            user.setTwoFactorEnabled(false);
            return userRepository.save(user);
        }
        return user;
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
