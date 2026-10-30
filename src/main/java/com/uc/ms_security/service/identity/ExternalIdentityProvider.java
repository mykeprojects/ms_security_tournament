package com.uc.ms_security.service.identity;

public interface ExternalIdentityProvider {

    boolean supports(String provider);

    ExternalIdentity verify(String token, String provider);
}
