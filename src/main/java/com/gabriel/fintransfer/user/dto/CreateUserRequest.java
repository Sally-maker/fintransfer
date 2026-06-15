package com.gabriel.fintransfer.user.dto;

import com.gabriel.fintransfer.user.domain.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{14}", message = "Must be a valid CPF (11 digits) or CNPJ (14 digits)") String cpfCnpj,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull UserType userType
) {}
