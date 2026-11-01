package com.uc.ms_security.mapper;

import com.uc.ms_security.dto.user.RoleAuthorizationDTO;
import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleAuthorizationMapperTest {

    private final RoleAuthorizationMapper mapper = new RoleAuthorizationMapper();

    @Test
    void groupsPermissionsByAssignedRole() {
        Role admin = role(1L, "ADMIN", "Administrador");
        Role consultor = role(2L, "CONSULTOR", "Consulta");
        Permission listUsers = permission(10L, "GET", "/api/users", "users");
        Permission createUsers = permission(11L, "POST", "/api/users", "users");
        Permission listRoles = permission(12L, "GET", "/api/roles", "roles");

        List<RoleAuthorizationDTO> result = mapper.toResponseDTOList(
                List.of(userRole(admin), userRole(consultor)),
                List.of(
                        rolePermission(admin, createUsers),
                        rolePermission(admin, listUsers),
                        rolePermission(consultor, listRoles)));

        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals(2, result.get(0).getPermissions().size());
        assertEquals("GET", result.get(0).getPermissions().get(0).getMethod());
        assertEquals("CONSULTOR", result.get(1).getName());
        assertEquals("/api/roles", result.get(1).getPermissions().get(0).getUrl());
    }

    @Test
    void returnsRoleWithoutPermissionsWhenNoneAreAssigned() {
        Role role = role(3L, "INVITADO", "Sin acceso");

        List<RoleAuthorizationDTO> result = mapper.toResponseDTOList(
                List.of(userRole(role)),
                List.of());

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPermissions().isEmpty());
    }

    private Role role(Long id, String name, String description) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setDescription(description);
        return role;
    }

    private Permission permission(Long id, String method, String url, String model) {
        Permission permission = new Permission();
        permission.setId(id);
        permission.setMethod(method);
        permission.setUrl(url);
        permission.setModel(model);
        return permission;
    }

    private UserRole userRole(Role role) {
        UserRole assignment = new UserRole();
        assignment.setRole(role);
        return assignment;
    }

    private RolePermission rolePermission(Role role, Permission permission) {
        RolePermission assignment = new RolePermission();
        assignment.setRole(role);
        assignment.setPermission(permission);
        return assignment;
    }
}
