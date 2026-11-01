package com.uc.ms_security.service;

import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolePermissionSetServiceTest {

    private RoleRepository roleRepository;
    private RolePermissionRepository rolePermissionRepository;
    private PermissionRepository permissionRepository;
    private RolePermissionSetService service;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        rolePermissionRepository = mock(RolePermissionRepository.class);
        permissionRepository = mock(PermissionRepository.class);
        service = new RolePermissionSetService(
                roleRepository,
                rolePermissionRepository,
                permissionRepository);
    }

    @Test
    void rejectsPermissionSetsThatMatchAnotherRoleRegardlessOfOrder() {
        when(roleRepository.findAll()).thenReturn(List.of(role(1L), role(2L)));
        when(rolePermissionRepository.findAllWithRole()).thenReturn(List.of(
                assignment(1L, 10L),
                assignment(1L, 11L),
                assignment(2L, 11L),
                assignment(2L, 10L)));

        assertThrows(
                ApplicationException.class,
                () -> service.assertUnique(null, Set.of(10L, 11L)));
    }

    @Test
    void allowsDifferentPermissionSetsAndExcludesTheEditedRole() {
        when(roleRepository.findAll()).thenReturn(List.of(role(1L)));
        when(rolePermissionRepository.findAllWithRole()).thenReturn(List.of(
                assignment(1L, 10L)));

        assertDoesNotThrow(
                () -> service.assertUnique(null, Set.of(10L, 11L)));
        assertDoesNotThrow(
                () -> service.assertUnique(1L, Set.of(10L)));
    }

    @Test
    void treatsEmptyPermissionSetsAsDuplicates() {
        when(roleRepository.findAll()).thenReturn(List.of(role(1L), role(2L)));
        when(rolePermissionRepository.findAllWithRole()).thenReturn(List.of());

        assertThrows(
                ApplicationException.class,
                () -> service.assertUnique(null, Set.of()));
    }

    @Test
    void rejectsPermissionIdsThatDoNotExist() {
        when(permissionRepository.findAllById(Set.of(404L))).thenReturn(List.of());

        assertThrows(
                ApplicationException.class,
                () -> service.validatePermissionIds(Set.of(404L)));
    }

    @Test
    void rejectsPermissionDeletionThatWouldCreateDuplicateSets() {
        when(roleRepository.findAll()).thenReturn(List.of(role(1L), role(2L)));
        when(rolePermissionRepository.findAllWithRole()).thenReturn(List.of(
                assignment(1L, 10L),
                assignment(1L, 11L),
                assignment(2L, 10L)));

        assertThrows(
                ApplicationException.class,
                () -> service.assertPermissionDeletionDoesNotDuplicateRoles(11L));
    }

    @Test
    void onlyAdminIsProtectedAndCanBeBackfilledWithAllPermissions() {
        Role admin = role(1L);
        admin.setName("ADMIN");
        Role administrador = role(2L);
        administrador.setName("ADMINISTRADOR");
        Permission permission = new Permission();
        permission.setId(10L);
        when(roleRepository.findAll()).thenReturn(List.of(admin, administrador));
        when(permissionRepository.findAll()).thenReturn(List.of(permission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(1L, 10L))
                .thenReturn(false);

        assertThrows(
                ApplicationException.class,
                () -> service.assertPermissionsModifiable(admin));
        assertDoesNotThrow(
                () -> service.assertPermissionsModifiable(administrador));
        service.ensureAdminHasAllPermissions();

        verify(rolePermissionRepository)
                .existsByRoleIdAndPermissionId(1L, 10L);
        verify(rolePermissionRepository, never())
                .existsByRoleIdAndPermissionId(2L, 10L);
        verify(rolePermissionRepository).saveAll(anyList());
    }

    @Test
    void refusesDeletingPermissionAssignedToAdmin() {
        Role admin = role(1L);
        admin.setName("ADMIN");
        Role administrador = role(2L);
        administrador.setName("ADMINISTRADOR");
        when(roleRepository.findAll()).thenReturn(List.of(admin, administrador));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(1L, 10L))
                .thenReturn(true);

        assertThrows(
                ApplicationException.class,
                () -> service.assertPermissionNotAssignedToAdmin(10L));

        verify(rolePermissionRepository, never())
                .existsByRoleIdAndPermissionId(2L, 10L);
    }

    private Role role(Long id) {
        Role role = new Role();
        role.setId(id);
        return role;
    }

    private RolePermission assignment(Long roleId, Long permissionId) {
        Permission permission = new Permission();
        permission.setId(permissionId);
        RolePermission assignment = new RolePermission();
        assignment.setRole(role(roleId));
        assignment.setPermission(permission);
        return assignment;
    }
}
