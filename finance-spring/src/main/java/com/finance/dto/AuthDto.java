package com.finance.dto;

import com.finance.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

// ─── Auth ─────────────────────────────────────────────────────────────────────
public class AuthDto {

    @Data
    public static class LoginRequest {
        @Email(message = "Valid email required")
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private UserDto.Response user;

        public LoginResponse(String token, UserDto.Response user) {
            this.token = token;
            this.user  = user;
        }
    }
}
