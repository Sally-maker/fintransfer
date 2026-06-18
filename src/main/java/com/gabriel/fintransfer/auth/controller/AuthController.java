package com.gabriel.fintransfer.auth.controller;

import com.gabriel.fintransfer.auth.dto.LoginRequest;
import com.gabriel.fintransfer.auth.dto.LoginResponse;
import com.gabriel.fintransfer.shared.security.JwtTokenProvider;
import com.gabriel.fintransfer.user.dto.CreateUserRequest;
import com.gabriel.fintransfer.user.dto.UserResponse;
import com.gabriel.fintransfer.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user and return JWT token")
    public LoginResponse register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.create(request);
        String token = tokenProvider.generateToken(request.email());
        return new LoginResponse(token, user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = tokenProvider.generateToken(request.email());
        UserResponse user = userService.findByEmail(request.email());
        return new LoginResponse(token, user);
    }
}
