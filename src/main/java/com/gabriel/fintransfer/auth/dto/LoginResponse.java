package com.gabriel.fintransfer.auth.dto;

import com.gabriel.fintransfer.user.dto.UserResponse;

public record LoginResponse(String token, UserResponse user) {}
