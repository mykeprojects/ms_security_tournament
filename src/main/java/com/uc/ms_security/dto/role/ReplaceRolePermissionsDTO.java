package com.uc.ms_security.dto.role;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ReplaceRolePermissionsDTO {
    @NotNull(message = "La lista de permisos es obligatoria")
    private Set<Long> permissionIds;
}
