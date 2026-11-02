package com.uc.ms_security.controller;

import com.uc.ms_security.dto.userrole.*;
import jakarta.validation.Valid;
import com.uc.ms_security.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
@RequiredArgsConstructor
public class UserRoleController {
    private final UserRoleService service;
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public UserRoleResponseDTO create(@Valid @RequestBody CreateUserRoleDTO dto) { return service.create(dto); }
    
    @GetMapping public List<UserRoleResponseDTO> findAll() { return service.findAll(); }
    
    @GetMapping("/users/{userId}") public List<UserRoleResponseDTO> findByUserId(@PathVariable Long userId) { return service.findByUserId(userId); }
    
    @GetMapping("/roles/{roleId}") public List<RoleUserResponseDTO> findByRoleId(@PathVariable Long roleId) { return service.findByRoleId(roleId); }
    
    @GetMapping("/{id}") public UserRoleResponseDTO findById(@PathVariable Long id) { return service.findById(id); }
    
    @DeleteMapping("/{userId}/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            Authentication authentication) {
        service.delete(userId, roleId, authenticatedUserId(authentication));
    }

    private long authenticatedUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "No tienes permiso para gestionar roles de usuarios.");
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException(
                    "No tienes permiso para gestionar roles de usuarios.");
        }
    }
}