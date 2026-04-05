package com.finance.dto;

import com.finance.entity.User;
import com.finance.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class CreateRequest {
        @NotBlank @Size(min = 2, max = 100)
        private String name;

        @Email @NotBlank
        private String email;

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        private Role role = Role.VIEWER;
    }

    @Data
    public static class UpdateRequest {
        @Size(min = 2, max = 100)
        private String name;

        private Role role;

        private Boolean active;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private Role role;
        private boolean active;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(User u) {
            Response r = new Response();
            r.id        = u.getId();
            r.name      = u.getName();
            r.email     = u.getEmail();
            r.role      = u.getRole();
            r.active    = u.isActive();
            r.createdAt = u.getCreatedAt();
            r.updatedAt = u.getUpdatedAt();
            return r;
        }
    }
}
