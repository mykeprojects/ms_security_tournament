package com.uc.ms_security.dto.user;

import com.uc.ms_security.dto.profile.ProfileResponseDTO;
import lombok.Value;

import java.util.List;

@Value
public class UserDetailResponseDTO {

    Long id;

    String name;

    String email;

    ProfileResponseDTO profile;

    List<RoleAuthorizationDTO> roles;
}