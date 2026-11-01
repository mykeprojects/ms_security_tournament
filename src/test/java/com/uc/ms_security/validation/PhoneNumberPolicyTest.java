package com.uc.ms_security.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumberPolicyTest {

    @Test
    void acceptsExactlyTenDigits() {
        assertTrue(PhoneNumberPolicy.matches("3001234567"));
    }

    @Test
    void rejectsLettersSpacesAndWrongLength() {
        assertFalse(PhoneNumberPolicy.matches(null));
        assertFalse(PhoneNumberPolicy.matches("30012abc67"));
        assertFalse(PhoneNumberPolicy.matches("300 123456"));
        assertFalse(PhoneNumberPolicy.matches("300123456"));
        assertFalse(PhoneNumberPolicy.matches("30012345678"));
    }
}
