package com.finance.controller;

import com.finance.dto.AuthDto;
import com.finance.dto.UserDto;
import com.finance.entity.User;
import com.finance.repository.UserRepository;
import com.finance.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        // Throws BadCredentialsException if wrong — handled by GlobalExceptionHandler
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user  = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String jwt = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthDto.LoginResponse(jwt, UserDto.Response.from(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto.Response> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.Response.from(user));
    }
}
