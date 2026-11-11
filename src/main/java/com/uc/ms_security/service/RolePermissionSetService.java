package com.uc.ms_security.service;

import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RolePermissionSetService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public Set<Long> validatePermissionIds(Set<Long> permissionIds) {
        Set<Long> ids = permissionIds == null
                ? new HashSet<>()
                : new HashSet<>(permissionIds);
        if (ids.contains(null)
                || permissionRepository.findAllById(ids).size() != ids.size()) {
            throw new ApplicationException(
                    ErrorCase.NOT_FOUND,
                    "Uno o más permisos seleccionados no existen");
        }
        return ids;
    }

    @Transactional(readOnly = true)
    public Set<Long> getPermissionIds(Long roleId) {
        Set<Long> permissionIds = new HashSet<>();
        for (RolePermission assignment : rolePermissionRepository.findByRoleId(roleId)) {
            permissionIds.add(assignment.getPermission().getId());
        }
        return permissionIds;
    }

    public boolean isAdministratorRoleName(String roleName) {
        return roleName != null
                && "ADMIN".equalsIgnoreCase(roleName.trim());
    }

    public boolean isAdministratorRole(Role role) {
        return role != null && isAdministratorRoleName(role.getName());
    }

    public void assertPermissionsModifiable(Role role) {
        if (isAdministratorRole(role)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "No se pueden modificar los permisos del rol administrador");
        }
    }

    @Transactional(readOnly = true)
    public void assertUnique(Long excludedRoleId, Set<Long> candidatePermissionIds) {
        Map<Long, Set<Long>> permissionIdsByRole = new HashMap<>();
        roleRepository.findAll().forEach(
                role -> permissionIdsByRole.put(role.getId(), new HashSet<>()));
        rolePermissionRepository.findAllWithRole().forEach(assignment ->
                permissionIdsByRole
                        .computeIfAbsent(
                                assignment.getRole().getId(),
                                ignored -> new HashSet<>())
                        .add(assignment.getPermission().getId()));

        boolean duplicate = permissionIdsByRole.entrySet().stream()
                .anyMatch(entry ->
                        !entry.getKey().equals(excludedRoleId)
                                && entry.getValue().equals(candidatePermissionIds));
        if (duplicate) {
            throw new ApplicationException(
                    ErrorCase.CONFLICT,
                    "Ya existe un rol con esa misma configuración de permisos");
        }
    }

    @Transactional
    public void replacePermissions(Role role, Set<Long> requestedPermissionIds) {
        assertPermissionsModifiable(role);
        Set<Long> permissionIds = validatePermissionIds(requestedPermissionIds);
        assertUnique(role.getId(), permissionIds);

        List<RolePermission> currentAssignments =
                rolePermissionRepository.findByRoleId(role.getId());
        Set<Long> currentPermissionIds = new HashSet<>();
        currentAssignments.forEach(assignment ->
                currentPermissionIds.add(assignment.getPermission().getId()));
        if (currentPermissionIds.equals(permissionIds)) {
            return;
        }

        rolePermissionRepository.deleteAll(currentAssignments);
        rolePermissionRepository.flush();

        Set<Permission> permissions = new HashSet<>(
                permissionRepository.findAllById(permissionIds));
        Set<RolePermission> assignments = new HashSet<>();
        for (Permission permission : permissions) {
            RolePermission assignment = new RolePermission();
            assignment.setRole(role);
            assignment.setPermission(permission);
            assignments.add(assignment);
        }
        rolePermissionRepository.saveAll(assignments);
    }

    @Transactional
    public void ensureAdminHasAllPermissions() {
        List<Role> administrators = roleRepository.findAll().stream()
                .filter(this::isAdministratorRole)
                .toList();
        List<Permission> permissions = permissionRepository.findAll();
        for (Role administrator : administrators) {
            List<RolePermission> missingAssignments = permissions.stream()
                    .filter(permission ->
                            !rolePermissionRepository.existsByRoleIdAndPermissionId(
                                    administrator.getId(), permission.getId()))
                    .map(permission -> assignment(administrator, permission))
                    .toList();
            if (!missingAssignments.isEmpty()) {
                rolePermissionRepository.saveAll(missingAssignments);
            }
        }
    }

    @Transactional
    public void grantPermissionToAdmin(Permission permission) {
        List<RolePermission> missingAssignments = roleRepository.findAll().stream()
                .filter(this::isAdministratorRole)
                .filter(administrator ->
                        !rolePermissionRepository.existsByRoleIdAndPermissionId(
                                administrator.getId(), permission.getId()))
                .map(administrator -> assignment(administrator, permission))
                .toList();
        if (!missingAssignments.isEmpty()) {
            rolePermissionRepository.saveAll(missingAssignments);
        }
    }

    @Transactional
    public void unassignPermissionFromAllRoles(Long permissionId) {
        rolePermissionRepository.deleteAllByPermissionId(permissionId);
        rolePermissionRepository.flush();
    }

    @Transactional(readOnly = true)
    public void assertPermissionNotAssignedToAdmin(Long permissionId) {
        boolean assignedToAdmin = roleRepository.findAll().stream()
                .filter(this::isAdministratorRole)
                .anyMatch(administrator ->
                        rolePermissionRepository.existsByRoleIdAndPermissionId(
                                administrator.getId(), permissionId));
        if (assignedToAdmin) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "No se puede eliminar un permiso asignado al rol ADMIN");
        }
    }

    private RolePermission assignment(Role role, Permission permission) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        return rolePermission;
    }

    @Transactional(readOnly = true)
    public void assertPermissionDeletionDoesNotDuplicateRoles(Long permissionId) {
        Map<Long, Set<Long>> beforeDeletion = new HashMap<>();
        Map<Long, Set<Long>> afterDeletion = new HashMap<>();
        roleRepository.findAll().forEach(role -> {
            beforeDeletion.put(role.getId(), new HashSet<>());
            afterDeletion.put(role.getId(), new HashSet<>());
        });
        rolePermissionRepository.findAllWithRole().forEach(assignment -> {
            Long roleId = assignment.getRole().getId();
            Long assignedPermissionId = assignment.getPermission().getId();
            beforeDeletion.computeIfAbsent(roleId, ignored -> new HashSet<>())
                    .add(assignedPermissionId);
            if (!assignedPermissionId.equals(permissionId)) {
                afterDeletion.computeIfAbsent(roleId, ignored -> new HashSet<>())
                        .add(assignedPermissionId);
            }
        });

        List<Long> roleIds = new ArrayList<>(afterDeletion.keySet());
        for (int first = 0; first < roleIds.size(); first++) {
            for (int second = first + 1; second < roleIds.size(); second++) {
                Long firstRoleId = roleIds.get(first);
                Long secondRoleId = roleIds.get(second);
                boolean newlyDuplicated = afterDeletion.get(firstRoleId)
                        .equals(afterDeletion.get(secondRoleId))
                        && !beforeDeletion.get(firstRoleId)
                                .equals(beforeDeletion.get(secondRoleId));
                if (newlyDuplicated) {
                    throw new ApplicationException(
                            ErrorCase.CONFLICT,
                            "No se puede eliminar el permiso porque dejaría dos roles con la misma configuración");
                }
            }
        }
    }
}
