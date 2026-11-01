package com.uc.ms_security.validation;

public final class PhoneNumberPolicy {

    public static final String PATTERN = "\\d{10}";
    public static final String MESSAGE =
            "El teléfono debe tener exactamente 10 dígitos y solo números";

    private PhoneNumberPolicy() {
    }

    public static boolean matches(String phone) {
        return phone != null && phone.matches(PATTERN);
    }
}
