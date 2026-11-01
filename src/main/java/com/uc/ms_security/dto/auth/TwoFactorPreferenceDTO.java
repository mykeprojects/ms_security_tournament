package com.uc.ms_security.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorPreferenceDTO {

    @NotNull(message = "Debes indicar si deseas habilitar o deshabilitar el segundo factor")
    private Boolean enabled;
}
