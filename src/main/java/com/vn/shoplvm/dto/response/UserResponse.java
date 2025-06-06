package com.vn.shoplvm.dto.response;

import com.vn.shoplvm.entity.Gender;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private Gender gender;
    private LocalDateTime birthday;
    private String role;
}

