package com.uc.ms_security.service.identity.firebase;

import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.service.identity.ExternalIdentity;
import com.uc.ms_security.service.identity.ExternalIdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FirebaseIdentityProvider implements ExternalIdentityProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(FirebaseIdentityProvider.class);
    private static final String JWKS_URI =
            "https://www.googleapis.com/service_accounts/v1/jwk/"
                    + "securetoken@system.gserviceaccount.com";

    private final String projectId;
    private JwtDecoder jwtDecoder;

    public FirebaseIdentityProvider(
            @Value("${security.firebase.project-id:}") String projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean supports(String provider) {
        return "firebase".equalsIgnoreCase(provider);
    }

    @Override
    public ExternalIdentity verify(String token, String provider) {
        if (projectId.isBlank()) {
            throw new ApplicationException(
                    ErrorCase.INTERNAL_ERROR,
                    "El proveedor externo no está configurado en el backend");
        }

        try {
            Jwt firebaseToken = getJwtDecoder().decode(token);
            String email = firebaseToken.getClaimAsString("email");
            if (email == null || email.isBlank()) {
                throw new ApplicationException(
                        ErrorCase.UNAUTHORIZED,
                        "El proveedor no entregó un correo electrónico válido");
            }

            return new ExternalIdentity(
                    provider.toLowerCase(),
                    firebaseToken.getSubject(),
                    email,
                    firebaseToken.getClaimAsString("name"));
        } catch (org.springframework.security.oauth2.jwt.JwtException exception) {
            logger.warn(
                    "Firebase no pudo validar el ID token. projectId={}, message={}",
                    projectId,
                    exception.getMessage());
            throw new ApplicationException(
                    ErrorCase.UNAUTHORIZED,
                    "El token del proveedor externo no es válido");
        }
    }

    private synchronized JwtDecoder getJwtDecoder() {
        if (jwtDecoder != null) {
            return jwtDecoder;
        }

        String issuer = "https://securetoken.google.com/" + projectId;
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(JWKS_URI)
                .build();
        decoder.setJwtValidator(new FirebaseTokenValidator(issuer, projectId));
        jwtDecoder = decoder;
        return jwtDecoder;
    }

    private record FirebaseTokenValidator(
            String issuer,
            String audience) implements OAuth2TokenValidator<Jwt> {

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            OAuth2TokenValidatorResult defaultValidation =
                    JwtValidators.createDefaultWithIssuer(issuer).validate(token);
            if (defaultValidation.hasErrors()) {
                return defaultValidation;
            }

            List<String> audiences = token.getAudience();
            if (audiences == null || !audiences.contains(audience)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(
                                "invalid_token",
                                "El token no pertenece al proyecto Firebase configurado",
                                null));
            }

            return OAuth2TokenValidatorResult.success();
        }
    }
}
