package com.uc.ms_security.mapper;

import com.uc.ms_security.dto.permission.PermissionResponseDTO;
import com.uc.ms_security.dto.role.RoleAuthorizationDTO;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleAuthorizationMapper {

    private final PermissionMapper permissionMapper;

    public List<RoleAuthorizationDTO> toRoleAuthorizationList(
            List<UserRole> assignments,
            List<RolePermission> rolePermissions) {
        Map<Long, List<PermissionResponseDTO>> permissionsByRole = rolePermissions.stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getRole().getId(),
                        Collectors.mapping(
                                assignment -> permissionMapper.toResponseDTO(
                                        assignment.getPermission()),
                                Collectors.toList())));

        return assignments.stream()
                .map(UserRole::getRole)
                .map(role -> toRoleAuthorization(role, permissionsByRole))
                .toList();
    }

    private RoleAuthorizationDTO toRoleAuthorization(
            Role role,
            Map<Long, List<PermissionResponseDTO>> permissionsByRole) {
        return new RoleAuthorizationDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                permissionsByRole.getOrDefault(role.getId(), List.of()));
    }
}
