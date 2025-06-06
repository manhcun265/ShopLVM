package com.vn.shoplvm.repository;

import com.vn.shoplvm.entity.PasswordResetToken;
import com.vn.shoplvm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteAllByUser(User user);
    void delete(PasswordResetToken resetToken);
}
