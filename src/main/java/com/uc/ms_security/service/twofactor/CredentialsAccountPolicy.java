package com.uc.ms_security.service.twofactor;

import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import org.springframework.stereotype.Component;

@Component
public class CredentialsAccountPolicy {

    private static final String CREDENTIALS_PROVIDER = "CREDENTIALS";
    private static final String SEEDED_ADMIN_EMAIL = "admin@uc.edu.co";

    public boolean appliesTo(User user) {
        return user.getAuthProvider() == null
                || CREDENTIALS_PROVIDER.equals(user.getAuthProvider());
    }

    public boolean canUseTwoFactor(User user) {
        return appliesTo(user) && !isSeededAdmin(user);
    }

    public boolean isSeededAdmin(User user) {
        return user.getEmail() != null
                && SEEDED_ADMIN_EMAIL.equalsIgnoreCase(user.getEmail().trim());
    }

    public void requireCredentialsAccount(User user) {
        if (!appliesTo(user)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "La autenticación de dos factores solo aplica a cuentas con correo y contraseña");
        }
    }

    public void requireTwoFactorEligible(User user) {
        requireCredentialsAccount(user);
        if (isSeededAdmin(user)) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "El administrador base no usa autenticación de dos factores");
        }
    }
}
