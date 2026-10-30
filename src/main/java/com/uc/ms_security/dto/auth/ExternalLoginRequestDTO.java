package com.uc.ms_security.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalLoginRequestDTO {

    @NotBlank(message = "El proveedor es obligatorio")
    private String provider;

    @NotBlank(message = "El token del proveedor es obligatorio")
    private String idToken;
}
