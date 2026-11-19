package com.uc.ms_security.integration.fans;

/**
 * Outbound port used by the security microservice to delegate fan creation
 * to the sports microservice. The sports service remains the owner of fan data.
 */
public interface FanProvisioningClient {

    void provision(FanProvisioningRequest request);

    void delete(Long externalUserId);
}
