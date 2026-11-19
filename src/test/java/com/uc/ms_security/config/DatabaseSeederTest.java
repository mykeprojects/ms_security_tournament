package com.uc.ms_security.config;

import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(rolePermissionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
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
    }
}
