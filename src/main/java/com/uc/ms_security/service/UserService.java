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
import com.uc.ms_security.mapper.UserMapper;
import com.uc.ms_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

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

    public UserDetailResponseDTO findByIdAndProfile(Long id) {
        User user = userRepository
                .findWithProfileById(id)
                .orElseThrow(
                        () -> new ApplicationException(
                                ErrorCase.NOT_FOUND,
                                "Usuario no encontrado con id: " + id
                        )
                );

        return userMapper.toDetailResponseDTO(user);
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
    public void delete(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
    }
}