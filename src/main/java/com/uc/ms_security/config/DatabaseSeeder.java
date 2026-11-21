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
            new PermissionSeed("GET", "/", "system"),
            new PermissionSeed("GET", "/api/users", "users"),
            new PermissionSeed("POST", "/api/users", "users"),
            new PermissionSeed("GET", "/api/users/{id}", "users"),
            new PermissionSeed("GET", "/api/users/{id}/detail", "users"),
            new PermissionSeed("GET", "/api/users/{id}/detail-with-sessions", "users"),
            new PermissionSeed("GET", "/api/users/{id}/detail-with-roles", "users"),
            new PermissionSeed("GET", "/api/users/{id}/permissions", "users"),
            new PermissionSeed("PUT", "/api/users/{id}", "users"),
            new PermissionSeed("DELETE", "/api/users/{id}", "users"),
            new PermissionSeed("GET", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("POST", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("PUT", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("DELETE", "/api/users/{id}/profile", "profiles"),
            new PermissionSeed("POST", "/api/users/{id}/sessions", "sessions"),
            new PermissionSeed("GET", "/api/users/{id}/sessions", "sessions"),
            new PermissionSeed("GET", "/api/users/{id}/sessions/{id}", "sessions"),
            new PermissionSeed("PUT", "/api/users/{id}/sessions/{id}", "sessions"),
            new PermissionSeed("DELETE", "/api/users/{id}/sessions/{id}", "sessions"),
            new PermissionSeed("GET", "/api/auth/2fa", "two-factor"),
            new PermissionSeed("PUT", "/api/auth/2fa", "two-factor"),
            new PermissionSeed("GET", "/api/user-roles", "user-roles"),
            new PermissionSeed("POST", "/api/user-roles", "user-roles"),
            new PermissionSeed("GET", "/api/user-roles/{id}", "user-roles"),
            new PermissionSeed("GET", "/api/user-roles/users/{id}", "user-roles"),
            new PermissionSeed("GET", "/api/user-roles/roles/{id}", "user-roles"),
            new PermissionSeed("DELETE", "/api/user-roles/{id}/{id}", "user-roles"),
            new PermissionSeed("GET", "/api/roles", "roles"),
            new PermissionSeed("POST", "/api/roles", "roles"),
            new PermissionSeed("GET", "/api/roles/{id}", "roles"),
            new PermissionSeed("PUT", "/api/roles/{id}", "roles"),
            new PermissionSeed("PUT", "/api/roles/{id}/permissions", "roles"),
            new PermissionSeed("DELETE", "/api/roles/{id}", "roles"),
            new PermissionSeed("GET", "/api/role-permissions", "role-permissions"),
            new PermissionSeed("POST", "/api/role-permissions", "role-permissions"),
            new PermissionSeed("GET", "/api/role-permissions/{id}", "role-permissions"),
            new PermissionSeed(
                    "DELETE",
                    "/api/role-permissions/{id}",
                    "role-permissions"),
            new PermissionSeed("GET", "/api/permissions", "permissions"),
            new PermissionSeed("POST", "/api/permissions", "permissions"),
            new PermissionSeed("GET", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("PUT", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("DELETE", "/api/permissions/{id}", "permissions"),
            new PermissionSeed("GET", "/payment-method", "payment-methods"),
            new PermissionSeed("POST", "/payment-method", "payment-methods"),
            new PermissionSeed("GET", "/payment-method/{id}", "payment-methods"),
            new PermissionSeed("PATCH", "/payment-method/{id}", "payment-methods"),
            new PermissionSeed("DELETE", "/payment-method/{id}", "payment-methods"),
            new PermissionSeed("GET", "/sports", "sports"),
            new PermissionSeed("POST", "/sports", "sports"),
            new PermissionSeed("GET", "/sports/{id}", "sports"),
            new PermissionSeed("PATCH", "/sports/{id}", "sports"),
            new PermissionSeed("DELETE", "/sports/{id}", "sports"),
            new PermissionSeed("GET", "/teams", "teams"),
            new PermissionSeed("POST", "/teams", "teams"),
            new PermissionSeed("GET", "/teams/{id}", "teams"),
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
            new PermissionSeed("POST", "/matches/{id}/start", "matches"),
            new PermissionSeed("POST", "/matches/{id}/finish", "matches"),
            new PermissionSeed("DELETE", "/matches/{id}", "matches"),
            new PermissionSeed("GET", "/matches/{id}/bets", "bets"),
            new PermissionSeed("POST", "/matches/{id}/settle", "bets"),
            new PermissionSeed("GET", "/bets/me", "bets"),
            new PermissionSeed("POST", "/bets", "bets"),
            new PermissionSeed("PATCH", "/bets/{id}/cancel", "bets"),
            new PermissionSeed("GET", "/fans/me", "fans"),
            new PermissionSeed("GET", "/fans/me/payment-methods", "fan-payment-methods"),
            new PermissionSeed("POST", "/fans/me/payment-methods", "fan-payment-methods"),
            new PermissionSeed("PATCH", "/fans/me/payment-methods/{id}", "fan-payment-methods"),
            new PermissionSeed("DELETE", "/fans/me/payment-methods/{id}", "fan-payment-methods"),
            new PermissionSeed("GET", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed("POST", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed(
                    "PATCH",
                    "/matches/{id}/annotations/{id}",
                    "annotations"),
            new PermissionSeed(
                    "DELETE",
                    "/matches/{id}/annotations/{id}",
                    "annotations"),
            new PermissionSeed("GET", "/matches/{id}/tracking", "tracking"),
            new PermissionSeed("GET", "/matches/{id}/tracking/history", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/start", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/players/{id}/position", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/ball/position", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/pause", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/resume", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/stop", "tracking")
    };

    private static final PermissionSeed[] SPORTS_ADMIN_PERMISSIONS = {
            new PermissionSeed("GET", "/sports", "sports"),
            new PermissionSeed("POST", "/sports", "sports"),
            new PermissionSeed("GET", "/sports/{id}", "sports"),
            new PermissionSeed("PATCH", "/sports/{id}", "sports"),
            new PermissionSeed("DELETE", "/sports/{id}", "sports"),
            new PermissionSeed("GET", "/teams", "teams"),
            new PermissionSeed("POST", "/teams", "teams"),
            new PermissionSeed("GET", "/teams/{id}", "teams"),
            new PermissionSeed("PATCH", "/teams/{id}", "teams"),
            new PermissionSeed("DELETE", "/teams/{id}", "teams"),
            new PermissionSeed("GET", "/teams/{id}/sports", "teams"),
            new PermissionSeed("POST", "/teams/{id}/sports", "teams"),
            new PermissionSeed("DELETE", "/teams/{id}/sports/{id}", "teams"),
            new PermissionSeed("GET", "/teams/{id}/roster", "teams"),
            new PermissionSeed("POST", "/teams/{id}/players", "teams"),
            new PermissionSeed("PATCH", "/contracts/{id}", "contracts"),
            new PermissionSeed("POST", "/contracts/{id}/release", "contracts"),
            new PermissionSeed("GET", "/players", "players"),
            new PermissionSeed("POST", "/players", "players"),
            new PermissionSeed("GET", "/players/{id}", "players"),
            new PermissionSeed("PATCH", "/players/{id}", "players"),
            new PermissionSeed("DELETE", "/players/{id}", "players"),
            new PermissionSeed("GET", "/players/{id}/profile", "players"),
            new PermissionSeed("POST", "/players/{id}/skills", "players"),
            new PermissionSeed("DELETE", "/players/{id}/skills/{id}", "players"),
            new PermissionSeed("GET", "/skills", "skills"),
            new PermissionSeed("POST", "/skills", "skills"),
            new PermissionSeed("GET", "/skills/{id}", "skills"),
            new PermissionSeed("PATCH", "/skills/{id}", "skills"),
            new PermissionSeed("DELETE", "/skills/{id}", "skills"),
            new PermissionSeed("GET", "/tournaments", "tournaments"),
            new PermissionSeed("POST", "/tournaments", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("PATCH", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("DELETE", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}/development", "tournaments"),
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
            new PermissionSeed("POST", "/matches/{id}/start", "matches"),
            new PermissionSeed("POST", "/matches/{id}/finish", "matches"),
            new PermissionSeed("DELETE", "/matches/{id}", "matches"),
            new PermissionSeed("GET", "/matches/{id}/bets", "bets"),
            new PermissionSeed("POST", "/matches/{id}/settle", "bets"),
            new PermissionSeed("GET", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed("POST", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed("PATCH", "/matches/{id}/annotations/{id}", "annotations"),
            new PermissionSeed("DELETE", "/matches/{id}/annotations/{id}", "annotations"),
            new PermissionSeed("GET", "/matches/{id}/tracking", "tracking"),
            new PermissionSeed("GET", "/matches/{id}/tracking/history", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/start", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/pause", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/resume", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/players/{id}/position", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/ball/position", "tracking"),
            new PermissionSeed("POST", "/matches/{id}/tracking/stop", "tracking")
    };

    private static final PermissionSeed[] FAN_PERMISSIONS = {
            new PermissionSeed("GET", "/matches", "matches"),
            new PermissionSeed("GET", "/matches/available", "matches"),
            new PermissionSeed("GET", "/matches/upcoming", "matches"),
            new PermissionSeed("GET", "/matches/{id}", "matches"),
            new PermissionSeed("GET", "/matches/{id}/annotations", "annotations"),
            new PermissionSeed("GET", "/matches/{id}/tracking", "tracking"),
            new PermissionSeed("GET", "/matches/{id}/tracking/history", "tracking"),
            new PermissionSeed("GET", "/tournaments/{id}/matches", "matches"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}/matches", "matches"),
            new PermissionSeed("GET", "/tournaments/{id}/phases", "tournament-phases"),
            new PermissionSeed("GET", "/tournaments/{id}/phases/{id}", "tournament-phases"),
            new PermissionSeed("GET", "/teams", "teams"),
            new PermissionSeed("GET", "/teams/{id}", "teams"),
            new PermissionSeed("GET", "/tournaments", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}", "tournaments"),
            new PermissionSeed("GET", "/tournaments/{id}/development", "tournaments"),
            new PermissionSeed("GET", "/payment-method", "payment-methods"),
            new PermissionSeed("GET", "/fans/me", "fans"),
            new PermissionSeed("GET", "/fans/me/payment-methods", "fan-payment-methods"),
            new PermissionSeed("POST", "/fans/me/payment-methods", "fan-payment-methods"),
            new PermissionSeed("PATCH", "/fans/me/payment-methods/{id}", "fan-payment-methods"),
            new PermissionSeed("DELETE", "/fans/me/payment-methods/{id}", "fan-payment-methods"),
            new PermissionSeed("GET", "/fans/me/followed-teams", "fans"),
            new PermissionSeed("POST", "/fans/me/followed-teams", "fans"),
            new PermissionSeed("DELETE", "/fans/me/followed-teams/{id}", "fans"),
            new PermissionSeed("GET", "/bets/me", "bets"),
            new PermissionSeed("POST", "/bets", "bets"),
            new PermissionSeed("PATCH", "/bets/{id}/cancel", "bets")
    };

    private static final PermissionRename[] CATALOG_PERMISSION_RENAMES = {
            new PermissionRename("GET", "/deportes", "/sports", "sports"),
            new PermissionRename("POST", "/deportes", "/sports", "sports"),
            new PermissionRename("GET", "/deportes/{id}", "/sports/{id}", "sports"),
            new PermissionRename("PATCH", "/deportes/{id}", "/sports/{id}", "sports"),
            new PermissionRename("DELETE", "/deportes/{id}", "/sports/{id}", "sports"),
            new PermissionRename("GET", "/equipos", "/teams", "teams"),
            new PermissionRename("POST", "/equipos", "/teams", "teams"),
            new PermissionRename("GET", "/equipos/{id}", "/teams/{id}", "teams"),
            new PermissionRename("PATCH", "/equipos/{id}", "/teams/{id}", "teams"),
            new PermissionRename("DELETE", "/equipos/{id}", "/teams/{id}", "teams"),
            new PermissionRename("GET", "/equipos/{id}/deportes", "/teams/{id}/sports", "teams"),
            new PermissionRename("POST", "/equipos/{id}/deportes", "/teams/{id}/sports", "teams"),
            new PermissionRename("DELETE", "/equipos/{id}/deportes/{id}", "/teams/{id}/sports/{id}", "teams"),
            new PermissionRename("GET", "/equipos/{id}/plantilla", "/teams/{id}/roster", "teams"),
            new PermissionRename("POST", "/equipos/{id}/jugadores", "/teams/{id}/players", "teams"),
            new PermissionRename("PATCH", "/vinculaciones/{id}", "/contracts/{id}", "contracts"),
            new PermissionRename("POST", "/vinculaciones/{id}/retirar", "/contracts/{id}/release", "contracts"),
            new PermissionRename("GET", "/jugadores", "/players", "players"),
            new PermissionRename("POST", "/jugadores", "/players", "players"),
            new PermissionRename("GET", "/jugadores/{id}", "/players/{id}", "players"),
            new PermissionRename("PATCH", "/jugadores/{id}", "/players/{id}", "players"),
            new PermissionRename("DELETE", "/jugadores/{id}", "/players/{id}", "players"),
            new PermissionRename("GET", "/jugadores/{id}/perfil", "/players/{id}/profile", "players"),
            new PermissionRename("POST", "/jugadores/{id}/habilidades", "/players/{id}/skills", "players"),
            new PermissionRename("DELETE", "/jugadores/{id}/habilidades/{id}", "/players/{id}/skills/{id}", "players"),
            new PermissionRename("GET", "/habilidades", "/skills", "skills"),
            new PermissionRename("POST", "/habilidades", "/skills", "skills"),
            new PermissionRename("GET", "/habilidades/{id}", "/skills/{id}", "skills"),
            new PermissionRename("PATCH", "/habilidades/{id}", "/skills/{id}", "skills"),
            new PermissionRename("DELETE", "/habilidades/{id}", "/skills/{id}", "skills")
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
            renameCatalogPermissions();
            Role adminRole = findOrCreateRole("ADMIN", "Administrador");
            Role consultantRole = findOrCreateRole(
                    "CONSULTOR",
                    "Consulta de usuarios");
            Role sportsAdminRole = findOrCreateRole(
                    "ADMINISTRADOR DEPORTIVO",
                    "Gestión deportiva y de partidos");
            Role fanRole = findOrCreateRole(
                    "AFICIONADO",
                    "Consulta de partidos y gestión de apuestas propias");
            User adminUser = findOrCreateUser();

            assignRole(adminUser, adminRole);
            seedDemoUser(
                    "Consultor",
                    "consultor@uc.edu.co",
                    "Consultor124!",
                    consultantRole);
            seedDemoUser(
                    "Administrador Deportivo",
                    "deportivo@uc.edu.co",
                    "Deportivo123!",
                    sportsAdminRole);
            seedDemoUser(
                    "Aficionado",
                    "aficionado@uc.edu.co",
                    "Aficionado123!",
                    fanRole);
            for (PermissionSeed seed : ADMIN_PERMISSIONS) {
                assignPermission(adminRole, findOrCreatePermission(seed));
            }
            for (PermissionSeed seed : SPORTS_ADMIN_PERMISSIONS) {
                assignPermission(sportsAdminRole, findOrCreatePermission(seed));
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

    private void seedDemoUser(
            String name,
            String email,
            String password,
            Role role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setName(name);
            created.setEmail(email);
            created.setPassword(passwordEncoder.encode(password));
            created.setTwoFactorEnabled(false);
            return userRepository.save(created);
        });
        assignRole(user, role);
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

    private void renameCatalogPermissions() {
        for (PermissionRename rename : CATALOG_PERMISSION_RENAMES) {
            permissionRepository.findByUrlAndMethod(rename.previousUrl(), rename.method())
                    .ifPresent(permission -> {
                        if (permissionRepository.findByUrlAndMethod(rename.url(), rename.method()).isPresent()) {
                            return;
                        }
                        permission.setUrl(rename.url());
                        permission.setModel(rename.model());
                        permissionRepository.save(permission);
                    });
        }
    }

    private Permission findOrCreatePermission(PermissionSeed seed) {
        Permission permission = permissionRepository
                .findByUrlAndMethod(seed.url(), seed.method())
                .orElseGet(Permission::new);
        permission.setUrl(seed.url());
        permission.setMethod(seed.method());
        permission.setModel(seed.model());
        return permissionRepository.save(permission);
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

    private record PermissionRename(String method, String previousUrl, String url, String model) {
    }
}
