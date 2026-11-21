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
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseSeederTest {

    @Test
    void seedsOneDemoUserForEachNonAdminRoleWithEncodedPassword() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        PermissionRepository permissionRepository = mock(PermissionRepository.class);
        UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
        RolePermissionRepository rolePermissionRepository =
                mock(RolePermissionRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        RolePermissionSetService rolePermissionSetService =
                mock(RolePermissionSetService.class);
        AtomicLong ids = new AtomicLong();
        List<User> savedUsers = new ArrayList<>();
        List<UserRole> savedUserRoles = new ArrayList<>();
        List<RolePermission> savedRolePermissions = new ArrayList<>();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(ids.incrementAndGet());
            savedUsers.add(user);
            return user;
        });
        when(roleRepository.findByNameIgnoreCase(any())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            role.setId(ids.incrementAndGet());
            return role;
        });
        when(permissionRepository.findByUrlAndMethod(any(), any()))
                .thenReturn(Optional.empty());
        when(permissionRepository.save(any(Permission.class))).thenAnswer(invocation -> {
            Permission permission = invocation.getArgument(0);
            permission.setId(ids.incrementAndGet());
            return permission;
        });
        when(userRoleRepository.existsByUserIdAndRoleId(any(), any())).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> {
            UserRole userRole = invocation.getArgument(0);
            savedUserRoles.add(userRole);
            return userRole;
        });
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(any(), any()))
                .thenReturn(false);
        when(rolePermissionRepository.save(any())).thenAnswer(invocation -> {
            RolePermission rolePermission = invocation.getArgument(0);
            savedRolePermissions.add(rolePermission);
            return rolePermission;
        });
        when(passwordEncoder.encode(any())).thenAnswer(invocation ->
                "encoded:" + invocation.getArgument(0, String.class));

        DatabaseSeeder seeder = new DatabaseSeeder(
                userRepository,
                roleRepository,
                permissionRepository,
                userRoleRepository,
                rolePermissionRepository,
                passwordEncoder,
                rolePermissionSetService);
        CommandLineRunner runner = seeder.seedDatabase();
        runner.run();

        assertEquals(4, savedUsers.size());
        assertEquals(
                List.of(
                        "consultor@uc.edu.co:encoded:Consultor124!",
                        "deportivo@uc.edu.co:encoded:Deportivo123!",
                        "aficionado@uc.edu.co:encoded:Aficionado123!"),
                savedUsers.stream()
                        .filter(user -> !user.getEmail().equals("admin@uc.edu.co"))
                        .map(user -> user.getEmail() + ":" + user.getPassword())
                        .toList());
        assertEquals(
                List.of(
                        "consultor@uc.edu.co:CONSULTOR",
                        "deportivo@uc.edu.co:ADMINISTRADOR DEPORTIVO",
                        "aficionado@uc.edu.co:AFICIONADO"),
                savedUserRoles.stream()
                        .filter(userRole -> !userRole.getUser().getEmail()
                                .equals("admin@uc.edu.co"))
                        .map(userRole -> userRole.getUser().getEmail()
                                + ":" + userRole.getRole().getName())
                        .toList());

        Set<String> sportsAdminPermissions = savedRolePermissions.stream()
                .filter(assignment -> assignment.getRole().getName()
                        .equals("ADMINISTRADOR DEPORTIVO"))
                .map(assignment -> assignment.getPermission().getMethod()
                        + " " + assignment.getPermission().getUrl())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(sportsAdminPermissions.containsAll(Set.of(
                "GET /tournaments/{id}/development",
                "GET /tournaments/{id}/registrations",
                "POST /tournaments/{id}/registrations",
                "DELETE /tournaments/{id}/registrations/{id}",
                "GET /tournaments/{id}/phases",
                "GET /tournaments/{id}/phases/{id}",
                "POST /tournaments/{id}/phases",
                "PATCH /tournaments/{id}/phases/{id}",
                "DELETE /tournaments/{id}/phases/{id}",
                "GET /tournaments/{id}/phases/{id}/groups",
                "POST /tournaments/{id}/phases/{id}/groups",
                "PATCH /tournaments/{id}/phases/{id}/groups/{id}",
                "DELETE /tournaments/{id}/phases/{id}/groups/{id}",
                "POST /tournaments/{id}/phases/{id}/groups/{id}/teams",
                "DELETE /tournaments/{id}/phases/{id}/groups/{id}/teams/{id}",
                "GET /tournaments/{id}/phases/{id}/brackets",
                "POST /tournaments/{id}/phases/{id}/brackets/generate",
                "POST /tournaments/{id}/phases/{id}/brackets",
                "PATCH /tournaments/{id}/phases/{id}/brackets/{id}/winner",
                "DELETE /tournaments/{id}/phases/{id}/brackets/{id}",
                "GET /tournaments/{id}/matches",
                "POST /tournaments/{id}/matches",
                "GET /tournaments/{id}/phases/{id}/matches",
                "POST /tournaments/{id}/phases/{id}/matches",
                "GET /matches/{id}/bets",
                "POST /matches/{id}/settle",
                "POST /matches/{id}/start",
                "POST /matches/{id}/finish",
                "POST /matches/{id}/tracking/pause",
                "POST /matches/{id}/tracking/resume")));
        Set<String> administratorPermissions = savedRolePermissions.stream()
                .filter(assignment -> assignment.getRole().getName().equals("ADMIN"))
                .map(assignment -> assignment.getPermission().getMethod()
                        + " " + assignment.getPermission().getUrl())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(administratorPermissions.containsAll(Set.of(
                "POST /matches/{id}/tracking/pause",
                "POST /matches/{id}/tracking/resume")));
        assertFalse(sportsAdminPermissions.stream()
                .anyMatch(permission -> permission.startsWith("GET /api/users")
                        || permission.startsWith("GET /api/roles")));

        Set<String> fanPermissions = savedRolePermissions.stream()
                .filter(assignment -> assignment.getRole().getName().equals("AFICIONADO"))
                .map(assignment -> assignment.getPermission().getMethod()
                        + " " + assignment.getPermission().getUrl())
                .collect(java.util.stream.Collectors.toSet());
        assertTrue(fanPermissions.containsAll(Set.of(
                "GET /teams",
                "GET /teams/{id}",
                "GET /tournaments",
                "GET /tournaments/{id}",
                "GET /matches",
                "GET /matches/{id}",
                "GET /matches/{id}/annotations",
                "GET /tournaments/{id}/matches")));
        assertFalse(fanPermissions.contains("POST /teams"));
        assertFalse(fanPermissions.contains("POST /tournaments"));
    }
}
