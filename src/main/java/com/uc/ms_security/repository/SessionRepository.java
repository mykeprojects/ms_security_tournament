package com.uc.ms_security.repository;

import com.uc.ms_security.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findAllByUserId(Long userId);

    Optional<Session> findByIdAndUserId(Long sessionId, Long userId);

    boolean existsByToken(String token);
    boolean existsByTokenAndIdNot(String token, Long id);

    Optional<Session> findByToken(String token);

    void deleteByToken(String token);

    void deleteAllByUserId(Long userId);
}