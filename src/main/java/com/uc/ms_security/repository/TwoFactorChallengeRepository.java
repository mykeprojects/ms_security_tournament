package com.uc.ms_security.repository;

import com.uc.ms_security.entity.TwoFactorChallenge;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TwoFactorChallengeRepository extends JpaRepository<TwoFactorChallenge, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<TwoFactorChallenge> findByChallengeTokenHash(String challengeTokenHash);

    void deleteAllByUserIdAndUsedFalse(Long userId);
}
