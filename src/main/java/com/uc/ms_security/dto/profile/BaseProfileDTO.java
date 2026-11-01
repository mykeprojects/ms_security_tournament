package com.uc.ms_security.dto.profile;

import com.uc.ms_security.validation.PhoneNumberPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BaseProfileDTO {
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = PhoneNumberPolicy.PATTERN, message = PhoneNumberPolicy.MESSAGE)
    private String phone;

    @Size(max = 2048, message = "La URL de la fotografía no puede superar 2048 caracteres")
    private String photoUrl;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate birthDate;
}
