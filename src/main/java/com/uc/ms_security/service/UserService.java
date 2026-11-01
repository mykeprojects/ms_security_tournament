package com.uc.ms_security.service;

import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UpdateUserDTO;
import com.uc.ms_security.dto.user.UserDetailResponseDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.dto.user.UserSessionsResponseDTO;
import com.uc.ms_security.dto.userrole.UserRolesResponseDTO;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.entity.RolePermission;
import com.uc.ms_security.entity.UserRole;
import com.uc.ms_security.mapper.UserMapper;
import com.uc.ms_security.repository.PasswordResetTokenRepository;
import com.uc.ms_security.repository.RolePermissionRepository;
import com.uc.ms_security.repository.TwoFactorChallengeRepository;
import com.uc.ms_security.repository.UserRepository;
import com.uc.ms_security.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service //decorador propio de los servicios
@RequiredArgsConstructor //Requiere constructor.
public class UserService {

    //inyección de variables de clase. Necesitamos una constante del repositorio
    private final UserRepository userRepository;
    // Necesitamos una constantee del UserMapper.
    //Patrón Singleton: Un objeto que va a ayudar a el servicio a funcionar.
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorChallengeRepository twoFactorChallengeRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public UserResponseDTO create(CreateUserDTO dto) { //Hace las validacions necesarias en este momento.
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ApplicationException(
                ErrorCase.ALREADY_EXISTS,
                    "Ya existe un usuario con este email"
            );
        }
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = userMapper.toEntity(dto, encodedPassword); //Lo paso a una entidad con el mapper
        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "Ya existe un usuario con este email");
        }
        //savedUser tiene id porque ahora me lo esta creando
        return userMapper.toResponseDTO(savedUser); //Me devuelve las fichas del usuario para que no me manda contraseñas
    }

    public User createExternal(
            String name,
            String email,
            String provider,
            String subject) {
        User user = new User();
        user.setName(name == null || name.isBlank() ? email : name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        user.setAuthProvider(provider);
        user.setExternalSubject(subject);
        return userRepository.saveAndFlush(user);
    }

    public List<UserResponseDTO> findAll() {
        List<User> users = userRepository.findAll(); //Busqueme todos los usuarios
        return userMapper.toResponseDTOList(users); //El mapper lo convierte en una lista de usuarios sin contraseñass
    }

    private User findUser(Long id) {
        return userRepository.findById(id) //buscamos un usuario en la base de datos
            .orElseThrow(() -> new ApplicationException(
                ErrorCase.NOT_FOUND,
                "Usuario no encontrado con id: " + id
                ));
    }
    //problema en el siguiente codigo: clase del martes
    public UserResponseDTO findById(Long id) {
        User user = findUser(id); //Llama a la función id user
        return userMapper.toResponseDTO(user);//responde con el usuario
    }

    @Transactional(readOnly = true)
    public UserDetailResponseDTO findByIdAndProfile(Long id) {
        User user = userRepository
                .findWithProfileById(id)
                .orElseThrow(
                        () -> new ApplicationException(
                                ErrorCase.NOT_FOUND,
                                "Usuario no encontrado con id: " + id
                        )
                );
        List<UserRole> assignments = userRoleRepository.findAllByUserId(id);
        List<Long> roleIds = assignments.stream()
                .map(assignment -> assignment.getRole().getId())
                .toList();
        List<RolePermission> rolePermissions = roleIds.isEmpty()
                ? List.of()
                : rolePermissionRepository.findAllByRoleIdIn(roleIds);

        return userMapper.toDetailResponseDTO(user, assignments, rolePermissions);
    }

    public UserSessionsResponseDTO findByIdAndSessions(Long id) {
        User user = userRepository.findWithSessionsById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Usuario no encontrado con id: " + id
                ));
        return userMapper.toSessionsResponseDTO(user);
    }

    public UserRolesResponseDTO findByIdAndRoles(Long id) {
        User user = userRepository.findWithRolesById(id)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCase.NOT_FOUND,
                        "Usuario no encontrado con id: " + id
                ));
        return userMapper.toRolesResponseDTO(user);
    }

    public UserResponseDTO update(Long id, UpdateUserDTO dto) {
        User user = findUser(id);
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new ApplicationException(
                ErrorCase.ALREADY_EXISTS,
                    "El email pertenece a otro usuario"
            );
        }
        userMapper.updateBasicData(dto, user); //Pasan valores de la función por referencia
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDTO(updatedUser);
    }
    @Transactional
    public void delete(Long id) {
        User user = findUser(id);
        twoFactorChallengeRepository.deleteAllByUserId(id);
        passwordResetTokenRepository.deleteAllByUserId(id);
        userRepository.delete(user);
    }
}