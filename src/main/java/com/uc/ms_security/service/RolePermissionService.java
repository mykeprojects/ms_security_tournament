package com.uc.ms_security.service;

import com.uc.ms_security.dto.rolepermission.*;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.exception.*;
import com.uc.ms_security.mapper.RolePermissionMapper;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.PermissionRepository;
import com.uc.ms_security.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class RolePermissionService {
    private final RolePermissionRepository repository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper mapper;
    private final RolePermissionSetService rolePermissionSetService;

    public RolePermissionService(
            RolePermissionRepository repository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionMapper mapper,
            RolePermissionSetService rolePermissionSetService) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.mapper = mapper;
        this.rolePermissionSetService = rolePermissionSetService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RolePermissionResponseDTO create(CreateRolePermissionDTO dto) {
        var role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Rol no encontrado con id: " + dto.getRoleId()));
        rolePermissionSetService.assertPermissionsModifiable(role);
        var permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Permiso no encontrado con id: " + dto.getPermissionId()));

        Set<Long> permissionIds =
                rolePermissionSetService.getPermissionIds(role.getId());
        if (!permissionIds.add(permission.getId())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "El rol ya tiene asignado ese permiso");
        }
        rolePermissionSetService.assertUnique(role.getId(), permissionIds);

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        return mapper.toResponseDTO(repository.save(rolePermission));
    }

    public List<RolePermissionResponseDTO> findAll() { return mapper.toResponseDTOList(repository.findAll()); }
    public RolePermissionResponseDTO findById(Long id) { return mapper.toResponseDTO(findEntity(id)); }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long id) {
        RolePermission assignment = findEntity(id);
        rolePermissionSetService.assertPermissionsModifiable(
                assignment.getRole());
        Set<Long> permissionIds =
                rolePermissionSetService.getPermissionIds(
                        assignment.getRole().getId());
        permissionIds.remove(assignment.getPermission().getId());
        rolePermissionSetService.assertUnique(
                assignment.getRole().getId(), permissionIds);
        repository.delete(assignment);
    }

    private RolePermission findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ApplicationException(ErrorCase.NOT_FOUND, "Relación rol-permiso no encontrada con id: " + id)); }
}