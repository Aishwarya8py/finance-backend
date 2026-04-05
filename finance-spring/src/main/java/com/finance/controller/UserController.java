package com.finance.controller;

import com.finance.dto.PagedResponse;
import com.finance.dto.UserDto;
import com.finance.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")        // entire controller is admin-only
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<PagedResponse<UserDto.Response>> list(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int limit) {

        var pageResult = userService.listAll(page, limit);
        return ResponseEntity.ok(PagedResponse.of(pageResult, UserDto.Response::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(UserDto.Response.from(userService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<UserDto.Response> create(@Valid @RequestBody UserDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.Response.from(userService.create(req)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateRequest req) {
        return ResponseEntity.ok(UserDto.Response.from(userService.update(id, req)));
    }
}
