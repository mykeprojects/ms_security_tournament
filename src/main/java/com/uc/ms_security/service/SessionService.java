package com.uc.ms_security.service;

import com.uc.ms_security.dto.session.SessionRequestDTO;
import com.uc.ms_security.dto.session.SessionResponseDTO;
import com.uc.ms_security.entity.Session;
import com.uc.ms_security.entity.User;
import com.uc.ms_security.exception.ApplicationException;
import com.uc.ms_security.exception.ErrorCase;
import com.uc.ms_security.mapper.SessionMapper;
import com.uc.ms_security.repository.SessionRepository;
import com.uc.ms_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SessionMapper sessionMapper;

    public SessionResponseDTO create(
            Long userId,
            SessionRequestDTO dto) {
        User user = findUser(userId);

        if (sessionRepository.existsByToken(dto.getToken())) {
            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "El token ya está registrado"
            );
        }

        Session session = sessionMapper.toEntity(dto);
        session.setUser(user);

        Session savedSession = sessionRepository.save(session);

        return sessionMapper.toResponseDTO(savedSession);
    }

    public List<SessionResponseDTO> findAllByUserId(Long userId) {
        findUser(userId);

        List<Session> sessions =
                sessionRepository.findAllByUserId(userId);

        return sessionMapper.toResponseDTOList(sessions);
    }

    public SessionResponseDTO findById(
            Long userId,
            Long sessionId) {

        return sessionMapper.toResponseDTO(
                findSession(userId, sessionId)
        );
    }

    public SessionResponseDTO update(
            Long userId,
            Long sessionId,
            SessionRequestDTO dto) {

        Session session = findSession(userId, sessionId);

        if (sessionRepository.existsByTokenAndIdNot(
                dto.getToken(),
                sessionId)) {

            throw new ApplicationException(
                    ErrorCase.ALREADY_EXISTS,
                    "El token ya está registrado"
            );
        }

        sessionMapper.updateEntity(dto, session);

        Session updatedSession = sessionRepository.save(session);

        return sessionMapper.toResponseDTO(updatedSession);
    }

    public void delete(
            Long userId,
            Long sessionId) {

        sessionRepository.delete(
                findSession(userId, sessionId)
        );
    }

    public void createForAuthentication(
            User user,
            String token,
            Instant expiration) {
        Session session = new Session();
        session.setToken(token);
        session.setExpiration(LocalDateTime.ofInstant(expiration, ZoneOffset.UTC));
        session.setUser(user);
        sessionRepository.save(session);
    }

    public boolean isActive(String token) {
        return sessionRepository.findByToken(token)
                .map(session -> session.getExpiration().isAfter(LocalDateTime.now(ZoneOffset.UTC)))
                .orElse(false);
    }

    @Transactional
    public void revokeByToken(String token) {
        sessionRepository.deleteByToken(token);
    }

    @Transactional
    public void revokeAllByUserId(Long userId) {
        sessionRepository.deleteAllByUserId(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new ApplicationException(
                                ErrorCase.NOT_FOUND,
                                "Usuario no encontrado con id: " + userId
                        )
                );
    }

    private Session findSession(
            Long userId,
            Long sessionId) {

        return sessionRepository
                .findByIdAndUserId(sessionId, userId)
                .orElseThrow(
                        () -> new ApplicationException(
                                ErrorCase.NOT_FOUND,
                                "Sesión no encontrada para este usuario"
                        )
                );
    }
}