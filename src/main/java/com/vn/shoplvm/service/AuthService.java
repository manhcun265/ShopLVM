package com.vn.shoplvm.service;

import com.vn.shoplvm.dto.request.auth.*;
import com.vn.shoplvm.dto.response.auth.AuthResponse;
import com.vn.shoplvm.dto.response.auth.TokenResponse;

public interface AuthService {
    TokenResponse refreshToken(TokenRefreshRequest request);
    void logout(String token);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);


}
