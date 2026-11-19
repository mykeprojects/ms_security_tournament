package com.uc.ms_security.service;

import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.integration.fans.FanProvisioningClient;
import com.uc.ms_security.integration.fans.FanProvisioningException;
import com.uc.ms_security.integration.fans.FanProvisioningRequest;
import com.uc.ms_security.service.registration.RegistrationService;
import com.uc.ms_security.service.registration.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    private UserRegistrationService userRegistrationService;
    private FanProvisioningClient fanProvisioningClient;
    private RegistrationService service;

    @BeforeEach
    void setUp() {
        userRegistrationService = mock(UserRegistrationService.class);
        fanProvisioningClient = mock(FanProvisioningClient.class);
        service = new RegistrationService(
                userRegistrationService,
                fanProvisioningClient);
    }

    @Test
    void createsTheFanAfterTheSecurityAccount() {
        CreateUserDTO dto = new CreateUserDTO();
        UserResponseDTO user = new UserResponseDTO(25L, "Ana Pérez", "ana@example.com");
        when(userRegistrationService.createCredentialUser(dto)).thenReturn(user);

        UserResponseDTO response = service.register(dto);

        assertEquals(user, response);
        verify(fanProvisioningClient).provision(
                new FanProvisioningRequest(25L, "ana@example.com", "Ana Pérez"));
        verify(userRegistrationService, never()).compensate(any());
    }

    @Test
    void compensatesTheAccountWhenTheFanCannotBeCreated() {
        CreateUserDTO dto = new CreateUserDTO();
        UserResponseDTO user = new UserResponseDTO(25L, "Ana Pérez", "ana@example.com");
        when(userRegistrationService.createCredentialUser(dto)).thenReturn(user);
        doThrow(new FanProvisioningException("down"))
                .when(fanProvisioningClient)
                .provision(any());

        assertThrows(FanProvisioningException.class, () -> service.register(dto));

        verify(fanProvisioningClient).delete(25L);
        verify(userRegistrationService).compensate(25L);
    }
}
