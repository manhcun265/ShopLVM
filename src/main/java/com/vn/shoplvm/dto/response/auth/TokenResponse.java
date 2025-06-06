package com.vn.shoplvm.dto.response.auth;

import com.vn.shoplvm.dto.response.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenResponse {
    String accessToken;
    String refreshToken;
    String tokenType = "Bearer";
    private UserResponse user;
}
