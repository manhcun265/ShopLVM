package com.vn.shoplvm.repository;

import com.vn.shoplvm.entity.Token;
import com.vn.shoplvm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    void deleteAllByUser(User user);
}

