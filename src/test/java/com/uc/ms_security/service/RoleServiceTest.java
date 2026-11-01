package com.uc.ms_security.service;

import com.uc.ms_security.dto.role.CreateRoleDTO;
import com.uc.ms_security.dto.role.UpdateRoleDTO;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.mapper.RoleMapper;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleServiceTest {

    private RoleRepository roleRepository;
    private RolePermissionSetService permissionSetService;
    private RoleService service;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        permissionSetService = mock(RolePermissionSetService.class);
        service = new RoleService(
                roleRepository,
                new RoleMapper(),
                mock(UserRoleRepository.class),
                permissionSetService);
    }

    @Test
    void rejectsCreationWhenSelectedPermissionSetAlreadyExists() {
        CreateRoleDTO dto = new CreateRoleDTO();
        dto.setName("ROLE_DUPLICATE");
        dto.setDescription("Role with duplicate permissions");
        dto.setPermissionIds(Set.of(10L, 11L));
        when(roleRepository.existsByNameIgnoreCase(dto.getName())).thenReturn(false);
        when(permissionSetService.validatePermissionIds(dto.getPermissionIds()))
                .thenReturn(dto.getPermissionIds());
        doThrow(duplicateSetException())
                .when(permissionSetService)
                .assertUnique(null, dto.getPermissionIds());

        assertThrows(ApplicationException.class, () -> service.create(dto));

        verify(roleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsUpdateWhenSelectedPermissionSetAlreadyExists() {
        Long roleId = 7L;
        Role existingRole = new Role();
        existingRole.setId(roleId);
        UpdateRoleDTO dto = new UpdateRoleDTO();
        dto.setName("ROLE_UPDATED");
        dto.setDescription("Updated role");
        dto.setPermissionIds(Set.of(10L, 11L));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), roleId))
                .thenReturn(false);
        when(permissionSetService.validatePermissionIds(dto.getPermissionIds()))
                .thenReturn(dto.getPermissionIds());
        doThrow(duplicateSetException())
                .when(permissionSetService)
                .assertUnique(roleId, dto.getPermissionIds());

        assertThrows(
                ApplicationException.class,
                () -> service.update(roleId, dto));

        verify(roleRepository, never()).save(existingRole);
    }

    @Test
    void checksCurrentPermissionSetWhenUpdatingRoleDetailsOnly() {
        Long roleId = 7L;
        Role existingRole = new Role();
        existingRole.setId(roleId);
        UpdateRoleDTO dto = new UpdateRoleDTO();
        dto.setName("ROLE_UPDATED");
        dto.setDescription("Updated role");
        Set<Long> currentPermissionIds = Set.of(10L, 11L);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot(dto.getName(), roleId))
                .thenReturn(false);
        when(roleRepository.save(existingRole)).thenReturn(existingRole);
        when(permissionSetService.getPermissionIds(roleId))
                .thenReturn(currentPermissionIds);

        service.update(roleId, dto);

        verify(permissionSetService).assertUnique(roleId, currentPermissionIds);
        verify(permissionSetService, never())
                .replacePermissions(existingRole, currentPermissionIds);
    }

    @Test
    void rejectsChangingAdminRolePermissionsThroughRoleUpdate() {
        Long roleId = 7L;
        Role admin = new Role();
        admin.setId(roleId);
        admin.setName("ADMIN");
        UpdateRoleDTO dto = new UpdateRoleDTO();
        dto.setName("ADMIN");
        dto.setDescription("Administrator");
        dto.setPermissionIds(Set.of(10L));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(admin));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot("ADMIN", roleId))
                .thenReturn(false);
        when(permissionSetService.isAdministratorRole(admin)).thenReturn(true);
        doThrow(duplicateSetException())
                .when(permissionSetService)
                .assertPermissionsModifiable(admin);

        assertThrows(
                ApplicationException.class,
                () -> service.update(roleId, dto));

        verify(roleRepository, never()).save(admin);
        verify(permissionSetService, never())
                .replacePermissions(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anySet());
    }

    @Test
    void rejectsRenamingAdminRoleToBypassPermissionProtection() {
        Long roleId = 7L;
        Role admin = new Role();
        admin.setId(roleId);
        admin.setName("ADMIN");
        UpdateRoleDTO dto = new UpdateRoleDTO();
        dto.setName("OTHER");
        dto.setDescription("Administrator");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(admin));
        when(roleRepository.existsByNameIgnoreCaseAndIdNot("OTHER", roleId))
                .thenReturn(false);
        when(permissionSetService.isAdministratorRole(admin)).thenReturn(true);
        when(permissionSetService.isAdministratorRoleName("OTHER"))
                .thenReturn(false);

        assertThrows(
                ApplicationException.class,
                () -> service.update(roleId, dto));

        verify(roleRepository, never()).save(admin);
    }

    private ApplicationException duplicateSetException() {
        return new ApplicationException(
                ErrorCase.CONFLICT,
                "Ya existe un rol con esa misma configuración de permisos");
    }
}
