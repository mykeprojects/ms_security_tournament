package com.uc.ms_security.mapper;

import com.uc.ms_security.dto.user.PermissionAuthorizationDTO;
import com.uc.ms_security.dto.user.RoleAuthorizationDTO;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.UserRole;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoleAuthorizationMapper {

    public List<RoleAuthorizationDTO> toResponseDTOList(
            List<UserRole> assignments,
            List<RolePermission> rolePermissions) {
        Map<Long, List<PermissionAuthorizationDTO>> permissionsByRoleId =
                rolePermissions.stream()
                        .collect(Collectors.groupingBy(
                                assignment -> assignment.getRole().getId(),
                                Collectors.mapping(
                                        this::toPermissionDTO,
                                        Collectors.toList())));

        return assignments.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toMap(
                        Role::getId,
                        role -> role,
                        (current, ignored) -> current,
                        LinkedHashMap::new))
                .values()
                .stream()
                .sorted(Comparator.comparing(
                        Role::getName,
                        String.CASE_INSENSITIVE_ORDER))
                .map(role -> new RoleAuthorizationDTO(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        sortPermissions(permissionsByRoleId.getOrDefault(
                                role.getId(),
                                List.of()))))
                .toList();
    }

    private PermissionAuthorizationDTO toPermissionDTO(RolePermission assignment) {
        var permission = assignment.getPermission();
        return new PermissionAuthorizationDTO(
                permission.getId(),
                permission.getUrl(),
                permission.getMethod(),
                permission.getModel());
    }

    private List<PermissionAuthorizationDTO> sortPermissions(
            List<PermissionAuthorizationDTO> permissions) {
        return permissions.stream()
                .sorted(Comparator
                        .comparing(PermissionAuthorizationDTO::getModel,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(PermissionAuthorizationDTO::getMethod,
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PermissionAuthorizationDTO::getUrl,
                                String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
