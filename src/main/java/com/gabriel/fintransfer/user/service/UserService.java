package com.gabriel.fintransfer.user.service;

import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.dto.CreateUserRequest;
import com.gabriel.fintransfer.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse findById(UUID id);

    List<UserResponse> findAll();

    UserResponse findByEmail(String email);

    User findEntityById(UUID id);
}
