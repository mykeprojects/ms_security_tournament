package com.uc.ms_security.repository;

import com.uc.ms_security.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findTopByUserIdAndUsedFalseOrderByIdDesc(Long userId);

    Optional<PasswordResetToken> findByResetTokenHashAndUsedFalse(String resetTokenHash);

    void deleteAllByUserId(Long userId);
}
