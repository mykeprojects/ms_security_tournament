package com.uc.ms_security.dto.profile;

import lombok.Value;
import java.time.LocalDate;

@Value
public class ProfileResponseDTO {
    Long id;
    Long userId;
    String phone;
    String photoUrl;
    LocalDate birthDate;
}