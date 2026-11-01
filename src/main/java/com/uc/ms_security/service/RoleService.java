package com.uc.ms_security.service;

import com.uc.ms_security.dto.role.*;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.exception.*;
import com.uc.ms_security.mapper.RoleMapper;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class RoleService {
    private final RoleRepository repository;
    private final RoleMapper mapper;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionSetService rolePermissionSetService;

    public RoleService(
            RoleRepository repository,
            RoleMapper mapper,
            UserRoleRepository userRoleRepository,
            RolePermissionSetService rolePermissionSetService) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionSetService = rolePermissionSetService;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RoleResponseDTO create(CreateRoleDTO dto) {
        if (repository.existsByNameIgnoreCase(dto.getName())) throw new ApplicationException(ErrorCase.ALREADY_EXISTS, "Ya existe un rol con ese nombre");
        if (rolePermissionSetService.isAdministratorRoleName(dto.getName())) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "El rol administrador está reservado y no se puede crear nuevamente");
        }
        Set<Long> permissionIds =
                rolePermissionSetService.validatePermissionIds(dto.getPermissionIds());
        rolePermissionSetService.assertUnique(null, permissionIds);
        Role role = repository.save(mapper.toEntity(dto));
        if (dto.getPermissionIds() != null) {
            rolePermissionSetService.replacePermissions(role, permissionIds);
        }
        return mapper.toResponseDTO(role);
    }
    
    public List<RoleResponseDTO> findAll() { return mapper.toResponseDTOList(repository.findAll()); }

    public RoleResponseDTO findById(Long id) { return mapper.toResponseDTO(findEntity(id)); }
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RoleResponseDTO update(Long id, UpdateRoleDTO dto) {
        Role role = findEntity(id);
        if (repository.existsByNameIgnoreCaseAndIdNot(dto.getName(), id)) throw new ApplicationException(ErrorCase.ALREADY_EXISTS, "El nombre pertenece a otro rol");
        boolean administrator = rolePermissionSetService.isAdministratorRole(role);
        if (administrator && dto.getPermissionIds() != null) {
            rolePermissionSetService.assertPermissionsModifiable(role);
        }
        if (administrator
                && !rolePermissionSetService.isAdministratorRoleName(dto.getName())) {
            throw new ApplicationException(
                    ErrorCase.INVALID_OPERATION,
                    "No se puede cambiar el nombre reservado del rol administrador");
        }
        Set<Long> permissionIds = null;
        if (!administrator || dto.getPermissionIds() != null) {
            permissionIds = dto.getPermissionIds() == null
                    ? rolePermissionSetService.getPermissionIds(id)
                    : rolePermissionSetService.validatePermissionIds(
                            dto.getPermissionIds());
            rolePermissionSetService.assertUnique(id, permissionIds);
        }
        mapper.updateEntity(dto, role);
        Role savedRole = repository.save(role);
        if (dto.getPermissionIds() != null) {
            rolePermissionSetService.replacePermissions(savedRole, permissionIds);
        }
        return mapper.toResponseDTO(savedRole);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void replacePermissions(Long id, Set<Long> permissionIds) {
        Role role = findEntity(id);
        rolePermissionSetService.replacePermissions(role, permissionIds);
    }
    public void delete(Long id) {
        Role role = findEntity(id);
        rolePermissionSetService.assertPermissionsModifiable(role);
        if (userRoleRepository.existsByRoleId(id)) {
            throw new ApplicationException(
                    ErrorCase.CONFLICT,
                    "No se puede eliminar un rol que está asignado"
            );
        }
        repository.delete(role);
    }
    
    private Role findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ApplicationException(ErrorCase.NOT_FOUND, "Rol no encontrado con id: " + id)); }
}