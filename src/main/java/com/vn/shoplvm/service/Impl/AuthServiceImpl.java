package com.vn.shoplvm.service.Impl;

import com.vn.shoplvm.dto.request.auth.*;
import com.vn.shoplvm.dto.response.auth.AuthResponse;
import com.vn.shoplvm.dto.response.auth.TokenResponse;
import com.vn.shoplvm.entity.PasswordResetToken;
import com.vn.shoplvm.entity.Role;
import com.vn.shoplvm.entity.Token;
import com.vn.shoplvm.entity.User;
import com.vn.shoplvm.mapper.UserMapper;
import com.vn.shoplvm.repository.PasswordResetTokenRepository;
import com.vn.shoplvm.repository.TokenRepository;
import com.vn.shoplvm.repository.UserRepository;
import com.vn.shoplvm.service.AuthService;
import com.vn.shoplvm.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token is invalid");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Delete old tokens
        tokenRepository.deleteAllByUser(user);

        // Save new refresh token
        Token newToken = new Token();
        newToken.setToken(newRefreshToken);
        newToken.setUser(user);
        newToken.setExpired(false);
        newToken.setRevoked(false);
        tokenRepository.save(newToken);

        log.info("New access token generated for user: {}", user.getName());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String token) {
        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        storedToken.setRevoked(true);
        storedToken.setExpired(true);
        tokenRepository.save(storedToken);
        log.info("User logged out and token revoked: {}", storedToken.getUser().getName());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email does not exist"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        // Delete old reset tokens
        passwordResetTokenRepository.deleteAllByUser(user);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset token generated for user: {} - Token: {}", user.getName(), token);
        // TODO: Send email with reset token here
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete used reset token
        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset successfully for user: {}", user.getName());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getName());

        return generateTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getName());
        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Delete old tokens
        tokenRepository.deleteAllByUser(user);

        // Save new refresh token
        Token token = new Token();
        token.setToken(refreshToken);
        token.setUser(user);
        token.setRevoked(false);
        token.setExpired(false);
        tokenRepository.save(token);

        log.info("Tokens generated for user: {}", user.getName());

        return new AuthResponse(accessToken, refreshToken, userMapper.toUserResponse(user));
    }
}