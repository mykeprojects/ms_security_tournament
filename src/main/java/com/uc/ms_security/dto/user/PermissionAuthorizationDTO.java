package com.uc.ms_security.dto.user;

import lombok.Value;

@Value
public class PermissionAuthorizationDTO {

    Long id;

    String url;

    String method;

    String model;
}
