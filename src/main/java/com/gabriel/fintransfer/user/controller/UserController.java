package com.gabriel.fintransfer.user.controller;

import com.gabriel.fintransfer.user.dto.CreateUserRequest;
import com.gabriel.fintransfer.user.dto.UserResponse;
import com.gabriel.fintransfer.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user and their wallet")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping
    @Operation(summary = "List all users")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find user by ID")
    public UserResponse findById(@PathVariable UUID id) {
        return userService.findById(id);
    }
}
