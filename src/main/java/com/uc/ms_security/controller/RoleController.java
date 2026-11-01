package com.uc.ms_security.controller;

import com.uc.ms_security.dto.role.*;
import com.uc.ms_security.service.RoleService;
import com.uc.ms_security.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService service;
    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponseDTO create(
            @Valid @RequestBody CreateRoleDTO dto,
            Authentication authentication) {
        if (dto.getPermissionIds() != null) {
            requirePermissionAssignmentAccess(authentication);
        }
        return service.create(dto);
    }

    @GetMapping public List<RoleResponseDTO> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public RoleResponseDTO findById(@PathVariable Long id) { return service.findById(id); }

    @PutMapping("/{id}")
    public RoleResponseDTO update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleDTO dto,
            Authentication authentication) {
        if (dto.getPermissionIds() != null) {
            requirePermissionAssignmentAccess(authentication);
        }
        return service.update(id, dto);
    }

    @PutMapping("/{id}/permissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replacePermissions(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceRolePermissionsDTO dto) {
        service.replacePermissions(id, dto.getPermissionIds());
    }

    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }

    private void requirePermissionAssignmentAccess(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "No tienes permiso para asignar permisos a roles.");
        }

        final long userId;
        try {
            userId = Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException(
                    "No tienes permiso para asignar permisos a roles.");
        }

        boolean canAssignPermissions =
                permissionService.hasPermission(
                        userId, "GET", "/api/role-permissions")
                        && permissionService.hasPermission(
                                userId, "POST", "/api/role-permissions")
                        && permissionService.hasPermission(
                                userId,
                                "DELETE",
                                "/api/role-permissions/1");
        if (!canAssignPermissions) {
            throw new AccessDeniedException(
                    "No tienes permiso para asignar permisos a roles.");
        }
    }
}