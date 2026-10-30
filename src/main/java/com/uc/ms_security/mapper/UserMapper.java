package com.uc.ms_security.mapper;
//El mapper hace un proceso de conversión:
//1.El mapper arma el objeto (en el dto vienen desarmados (JSON)) (de dto a entity)
//2.Pasarlo de entity a dto.
//3.Arma relaciones

//Importa la entidad y los DTO.
import com.uc.ms_security.dto.user.CreateUserDTO;
import com.uc.ms_security.dto.user.UpdateUserDTO;
import com.uc.ms_security.dto.user.UserDetailResponseDTO;
import com.uc.ms_security.dto.user.UserResponseDTO;
import com.uc.ms_security.dto.user.UserSessionsResponseDTO;
import com.uc.ms_security.dto.userrole.UserRolesResponseDTO;
import com.uc.ms_security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component indica que es reutilizable
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ProfileMapper profileMapper;
    private final SessionMapper sessionMapper;
    private final UserRoleMapper userRoleMapper;

    //Transforma el DTO a Entity
    public User toEntity(CreateUserDTO dto, String encodedPassword) {
        User user = new User();//Crea la entidad para manipula la tabla

        user.setName(dto.getName()); //Le asignamos los atributos
        user.setEmail(dto.getEmail()); //Le asignamos los atributos
        user.setPassword(encodedPassword); //La contraseña ya fue protegida por el servicio
        user.setAuthProvider("CREDENTIALS");

        return user;
    }

    public void updateBasicData(UpdateUserDTO dto, User user) {
        //Hace las modificacions al usuario
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
    }

    //Cuando la respuesta va a salir se necesita este metodo.
    public UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO( //Ocultar datos
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public UserDetailResponseDTO toDetailResponseDTO(User user) {
        return new UserDetailResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profileMapper.toResponseDTO(user.getProfile())//Arrastra la relación
        );
    }

    public UserSessionsResponseDTO toSessionsResponseDTO(User user) {
        return new UserSessionsResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                sessionMapper.toResponseDTOList(user.getSessions())//En este punto se pide el join
        );
    }

    public UserRolesResponseDTO toRolesResponseDTO(User user) {
        return new UserRolesResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                userRoleMapper.toResponseDTOList(user.getUserRoles())
        );
    }

    //Ver todos los usuarios
    //map: mapea una lista
    public List<UserResponseDTO> toResponseDTOList(List<User> users) {
        return users.stream()
                .map(this::toResponseDTO)
                .toList();//Funciones propias de java para hacer procesos rápidos (velocidad y sintaxis)
    }
}