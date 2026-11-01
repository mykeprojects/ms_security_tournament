package com.uc.ms_security.dto.user;

import lombok.Value;

import java.util.List;

@Value
public class RoleAuthorizationDTO {

    Long id;

    String name;

    String description;

    List<PermissionAuthorizationDTO> permissions;
}
