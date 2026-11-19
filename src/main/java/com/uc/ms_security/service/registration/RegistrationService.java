package com.uc.ms_security.service.registration;

import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.integration.fans.FanProvisioningClient;
import com.uc.ms_security.integration.fans.FanProvisioningException;
import com.uc.ms_security.integration.fans.FanProvisioningRequest;
import com.uc.ms_security.service.identity.ExternalIdentity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Registration saga: creates the security account and delegates the fan
 * profile to the sports microservice. If the remote step fails, the newly
 * created account is compensated so no account is left without a fan profile.
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final Logger logger =
            LoggerFactory.getLogger(RegistrationService.class);

    private final UserRegistrationService userRegistrationService;
    private final FanProvisioningClient fanProvisioningClient;

    public UserResponseDTO register(CreateUserDTO dto) {
        UserResponseDTO user = userRegistrationService.createCredentialUser(dto);
        provisionFan(user.getId(), user.getName(), user.getEmail());
        return user;
    }

    public User registerExternal(ExternalIdentity identity) {
        User user = userRegistrationService.createExternalUser(identity);
        provisionFan(user.getId(), user.getName(), user.getEmail());
        return user;
    }

    private void provisionFan(Long userId, String fullName, String email) {
        try {
            fanProvisioningClient.provision(
                    new FanProvisioningRequest(userId, email, fullName));
        } catch (RuntimeException exception) {
            logger.error(
                    "No se pudo crear el aficionado para el usuario {}. Se compensará la cuenta.",
                    userId,
                    exception);
            compensate(userId);
            if (exception instanceof FanProvisioningException fanException) {
                throw fanException;
            }
            throw new FanProvisioningException(
                    "No fue posible crear el perfil de aficionado en el sistema deportivo",
                    exception);
        }
    }

    private void compensate(Long userId) {
        try {
            fanProvisioningClient.delete(userId);
        } catch (RuntimeException exception) {
            logger.error(
                    "Compensación incompleta: no se pudo eliminar el aficionado del usuario {}. "
                            + "Requiere reconciliación manual.",
                    userId,
                    exception);
        }
        try {
            userRegistrationService.compensate(userId);
        } catch (RuntimeException exception) {
            logger.error(
                    "Compensación incompleta: no se pudo eliminar la cuenta {} del sistema de seguridad.",
                    userId,
                    exception);
        }
    }
}
