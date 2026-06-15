package com.gabriel.fintransfer.user.dto;

import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.domain.UserType;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email, UserType userType) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getUserType());
    }
}
