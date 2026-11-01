package com.uc.ms_security.service;

import com.uc.ms_security.dto.permission.CreatePermissionDTO;
import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.mapper.PermissionMapper;
import com.uc.ms_security.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    private PermissionRepository repository;
    private RolePermissionSetService permissionSetService;
    private PermissionService service;

    @BeforeEach
    void setUp() {
        repository = mock(PermissionRepository.class);
        permissionSetService = mock(RolePermissionSetService.class);
        service = new PermissionService(
                repository,
                new PermissionMapper(),
                permissionSetService);
    }

    @Test
    void grantsEveryNewPermissionToAdminWhenCreated() {
        CreatePermissionDTO dto = new CreatePermissionDTO();
        dto.setUrl("/api/reports");
        dto.setMethod("get");
        dto.setModel("reports");
        when(repository.save(any(Permission.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        service.create(dto);

        verify(permissionSetService).grantPermissionToAdmin(
                org.mockito.ArgumentMatchers.argThat(permission ->
                        "/api/reports".equals(permission.getUrl())
                                && "GET".equals(permission.getMethod())));
    }
}
