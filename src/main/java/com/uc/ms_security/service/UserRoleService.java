package com.uc.ms_security.service;

import com.uc.ms_security.dto.userrole.*;
import com.uc.ms_security.entity.Role;
import com.uc.ms_security.entity.UserRole;
import com.uc.ms_security.exception.*;
import com.uc.ms_security.mapper.UserRoleMapper;
import com.uc.ms_security.repository.UserRoleRepository;
import com.uc.ms_security.repository.RoleRepository;
import com.uc.ms_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private static final List<String> ADMINISTRATOR_ROLE_NAMES =
            List.of("ADMIN", "ADMINISTRADOR");

    private final UserRoleRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper mapper;

    @Transactional
    public UserRoleResponseDTO create(CreateUserRoleDTO dto) {
        if (repository.existsByUserIdAndRoleId(dto.getUserId(), dto.getRoleId())) {
            throw new ApplicationException(
                    ErrorCase.CONFLICT,
                    "El usuario ya tiene asignado ese rol"
            );
        }
        UserRole userRole = new UserRole();
        userRole.setUser(userRepository.findById(dto.getUserId()).orElseThrow(() -> new ApplicationException(
            ErrorCase.NOT_FOUND, "Usuario no encontrado con id: " + dto.getUserId())));
        userRole.setRole(roleRepository.findById(dto.getRoleId()).orElseThrow(() -> new ApplicationException(
            ErrorCase.NOT_FOUND, "Rol no encontrado con id: " + dto.getRoleId())));
        return mapper.toResponseDTO(repository.save(userRole));
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponseDTO> findAll() {
        return mapper.toResponseDTOList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponseDTO> findByUserId(Long userId) {
        return mapper.toResponseDTOList(repository.findAllByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<RoleUserResponseDTO> findByRoleId(Long roleId) {
        return mapper.toRoleUserResponseDTOList(repository.findAllByRoleId(roleId));
    }

    @Transactional(readOnly = true)
    public UserRoleResponseDTO findById(Long id) {
        return mapper.toResponseDTO(findEntity(id));
    }

    @Transactional
    public void delete(Long userId, Long roleId, Long authenticatedUserId) {
        UserRole assignment = repository.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Asignación de rol no encontrada"
                ));
        if (isProtectedAdministratorRemoval(
                assignment, userId, authenticatedUserId)) {
            throw new ApplicationException(
                    ErrorCase.CONFLICT,
                    "No puedes quitar el rol de administrador a otro usuario que también tiene ese rol."
            );
        }
        repository.delete(assignment);
    }

    private boolean isProtectedAdministratorRemoval(
            UserRole assignment,
            Long targetUserId,
            Long authenticatedUserId) {
        if (authenticatedUserId.equals(targetUserId)
                || !isAdministratorRole(assignment.getRole())) {
            return false;
        }

        return repository.findAllByUserId(authenticatedUserId).stream()
                .anyMatch(userRole -> isAdministratorRole(userRole.getRole()));
    }

    private boolean isAdministratorRole(Role role) {
        return role != null
                && ADMINISTRATOR_ROLE_NAMES.stream()
                        .anyMatch(name -> name.equalsIgnoreCase(role.getName().trim()));
    }

    private UserRole findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new ApplicationException(ErrorCase.NOT_FOUND, "Relación usuario-rol no encontrada con id: " + id)); }
}