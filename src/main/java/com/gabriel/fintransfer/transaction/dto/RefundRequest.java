package com.gabriel.fintransfer.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RefundRequest(
        @NotNull UUID requesterId,
        @NotBlank String reason
) {}
