package com.uc.ms_security.service;

import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.entity.UserRole;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.mapper.UserRoleMapper;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRoleServiceTest {

    private UserRoleRepository repository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private UserRoleService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRoleRepository.class);
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        service = new UserRoleService(
                repository,
                userRepository,
                roleRepository,
                mock(UserRoleMapper.class));
    }

    @Test
    void administratorCannotRemoveAdministratorRoleFromAnotherAdministrator() {
        UserRole targetAssignment = assignment(2L, "ADMIN");
        when(repository.findByUserIdAndRoleId(2L, 10L))
                .thenReturn(Optional.of(targetAssignment));
        when(repository.findAllByUserId(1L)).thenReturn(List.of(assignment(1L, "ADMIN")));

        assertThrows(
                ApplicationException.class,
                () -> service.delete(2L, 10L, 1L));

        verify(repository, never()).delete(targetAssignment);
    }

    @Test
    void administratorCanRemoveOtherRolesFromAnotherAdministrator() {
        UserRole targetAssignment = assignment(2L, "USUARIO");
        when(repository.findByUserIdAndRoleId(2L, 10L))
                .thenReturn(Optional.of(targetAssignment));
        when(repository.findAllByUserId(1L)).thenReturn(List.of(assignment(1L, "ADMIN")));

        assertDoesNotThrow(() -> service.delete(2L, 10L, 1L));

        verify(repository).delete(targetAssignment);
    }

    @Test
    void nonAdministratorCanRemoveAdministratorRole() {
        UserRole targetAssignment = assignment(2L, "ADMIN");
        when(repository.findByUserIdAndRoleId(2L, 10L))
                .thenReturn(Optional.of(targetAssignment));
        when(repository.findAllByUserId(1L))
                .thenReturn(List.of(assignment(1L, "USUARIO")));

        assertDoesNotThrow(() -> service.delete(2L, 10L, 1L));

        verify(repository).delete(targetAssignment);
    }

    @Test
    void protectionAlsoRecognizesTheAdministratorRoleAlias() {
        UserRole targetAssignment = assignment(2L, "ADMINISTRADOR");
        when(repository.findByUserIdAndRoleId(2L, 10L))
                .thenReturn(Optional.of(targetAssignment));
        when(repository.findAllByUserId(1L)).thenReturn(List.of(assignment(1L, "ADMIN")));

        assertThrows(
                ApplicationException.class,
                () -> service.delete(2L, 10L, 1L));

        verify(repository, never()).delete(targetAssignment);
    }

    @Test
    void assignsTheDefaultRoleOnlyWhenItIsMissing() {
        User user = new User();
        user.setId(7L);
        Role role = new Role();
        role.setId(20L);
        role.setName("AFICIONADO");
        when(roleRepository.findByNameIgnoreCase("AFICIONADO"))
                .thenReturn(Optional.of(role));
        when(repository.existsByUserIdAndRoleId(7L, 20L)).thenReturn(false);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        service.assignIfMissing(7L, "AFICIONADO");

        verify(repository).save(any(UserRole.class));
    }

    @Test
    void doesNotDuplicateAnExistingDefaultRoleAssignment() {
        Role role = new Role();
        role.setId(20L);
        role.setName("AFICIONADO");
        when(roleRepository.findByNameIgnoreCase("AFICIONADO"))
                .thenReturn(Optional.of(role));
        when(repository.existsByUserIdAndRoleId(7L, 20L)).thenReturn(true);

        service.assignIfMissing(7L, "AFICIONADO");

        verify(repository, never()).save(any(UserRole.class));
    }

    private UserRole assignment(Long userId, String roleName) {
        User user = new User();
        user.setId(userId);
        Role role = new Role();
        role.setName(roleName);
        UserRole assignment = new UserRole();
        assignment.setUser(user);
        assignment.setRole(role);
        return assignment;
    }
}
