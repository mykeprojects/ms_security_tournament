package com.uc.ms_security.service;

import com.uc.ms_security.dto.permission.*;
import com.uc.ms_security.entity.Permission;
import com.uc.ms_security.exception.*;
import com.uc.ms_security.mapper.PermissionMapper;
import com.uc.ms_security.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository repository;
    private final PermissionMapper mapper;
    private final RolePermissionSetService rolePermissionSetService;

    @Transactional
    public PermissionResponseDTO create(CreatePermissionDTO dto) {
        String url = normalizeStoredUrl(dto.getUrl());
        String method = normalizeMethod(dto.getMethod());
        if (repository.existsByUrlAndMethod(url, method)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "Ya existe un permiso para ese método y URL");
        }
        Permission permission = mapper.toEntity(dto);
        permission.setUrl(url);
        permission.setMethod(method);
        Permission savedPermission = repository.save(permission);
        rolePermissionSetService.grantPermissionToAdmin(savedPermission);
        return mapper.toResponseDTO(savedPermission);
    }
    public List<PermissionResponseDTO> findAll() { return mapper.toResponseDTOList(repository.findAll()); }
    public List<PermissionResponseDTO> findAllByUserId(Long userId) {
        return mapper.toResponseDTOList(repository.findAllByUserId(userId));
    }
    public PermissionResponseDTO findById(Long id) { return mapper.toResponseDTO(findEntity(id)); }
    public PermissionResponseDTO update(Long id, UpdatePermissionDTO dto) {
        Permission permission = findEntity(id);
        String url = normalizeStoredUrl(dto.getUrl());
        String method = normalizeMethod(dto.getMethod());
        if (repository.existsByUrlAndMethodAndIdNot(url, method, id)) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "Ya existe un permiso para ese método y URL");
        }
        mapper.updateEntity(dto, permission);
        permission.setUrl(url);
        permission.setMethod(method);
        return mapper.toResponseDTO(repository.save(permission));
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long id) {
        findEntity(id);
        rolePermissionSetService.assertPermissionNotAssignedToAdmin(id);
        rolePermissionSetService.assertPermissionDeletionDoesNotDuplicateRoles(id);
        repository.deleteById(id);
    }

    public boolean hasPermission(Long userId, String httpMethod, String requestUrl) {
        if (userId == null || httpMethod == null || requestUrl == null) {
            return false;
        }
        return repository.findByUserIdAndMethodAndUrl(
                        userId,
                        normalizeMethod(httpMethod),
                        normalizeRequestUrl(requestUrl))
                .stream()
                .findAny()
                .isPresent();
    }

    private String normalizeMethod(String method) {
        return method.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStoredUrl(String url) {
        return url.trim();
    }

    private String normalizeRequestUrl(String requestUrl) {
        return requestUrl.replaceAll(
                "(?i)(?<=/)(?:\\d+|[a-f0-9]{24})(?=/|$)",
                "{id}");
    }

    private Permission findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ApplicationException(ErrorCase.NOT_FOUND, "Permiso no encontrado con id: " + id)); }
}