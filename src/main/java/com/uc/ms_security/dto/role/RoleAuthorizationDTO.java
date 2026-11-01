package com.uc.ms_security.dto.role;

import com.uc.ms_security.dto.permission.PermissionResponseDTO;
import lombok.Value;

import java.util.List;

@Value
public class RoleAuthorizationDTO {
    Long id;
    String name;
    String description;
    List<PermissionResponseDTO> permissions;
}
