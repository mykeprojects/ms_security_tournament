package com.uc.ms_security.integration.fans;

public class FanProvisioningException extends RuntimeException {

    public FanProvisioningException(String message) {
        super(message);
    }

    public FanProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
