package com.uc.ms_security.mapper;

import com.uc.ms_security.dto.role.RoleAuthorizationDTO;
import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleAuthorizationMapperTest {

    private final RoleAuthorizationMapper mapper =
            new RoleAuthorizationMapper(new PermissionMapper());

    @Test
    void groupsPermissionsByAssignedRole() {
        Role admin = role(1L, "ADMIN", "Administrador");
        Role consultant = role(2L, "CONSULTOR", "Consulta");
        Permission listUsers = permission(10L, "GET", "/api/users", "users");

        List<RoleAuthorizationDTO> roles = mapper.toRoleAuthorizationList(
                List.of(assignment(admin), assignment(consultant)),
                List.of(rolePermission(admin, listUsers)));

        assertEquals(2, roles.size());
        assertEquals("ADMIN", roles.get(0).getName());
        assertEquals(1, roles.get(0).getPermissions().size());
        assertEquals("/api/users", roles.get(0).getPermissions().get(0).getUrl());
        assertTrue(roles.get(1).getPermissions().isEmpty());
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

    private UserRole assignment(Role role) {
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        return userRole;
    }

    private RolePermission rolePermission(Role role, Permission permission) {
        RolePermission assignment = new RolePermission();
        assignment.setRole(role);
        assignment.setPermission(permission);
        return assignment;
    }
}
