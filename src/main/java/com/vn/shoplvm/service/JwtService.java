package com.vn.shoplvm.service;

import com.vn.shoplvm.entity.User;

public interface JwtService {
    String extractUsername(String token);

    boolean isTokenValid(String token, String username);

    String generateAccessToken(String username);

    String generateRefreshToken(String username);

    String generateAccessToken(User user);

    String generateRefreshToken(User user);
}
