package com.finance.service;

import com.finance.dto.UserDto;
import com.finance.entity.User;
import com.finance.exception.GlobalExceptionHandler.ConflictException;
import com.finance.exception.GlobalExceptionHandler.ResourceNotFoundException;
import com.finance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<User> listAll(int page, int limit) {
        return userRepository.findAll(
            PageRequest.of(page - 1, limit, Sort.by("createdAt").descending())
        );
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public User create(UserDto.CreateRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already registered: " + req.getEmail());
        }
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserDto.UpdateRequest req) {
        User user = findById(id);
        if (req.getName()   != null) user.setName(req.getName());
        if (req.getRole()   != null) user.setRole(req.getRole());
        if (req.getActive() != null) user.setActive(req.getActive());
        return userRepository.save(user);
    }
}
