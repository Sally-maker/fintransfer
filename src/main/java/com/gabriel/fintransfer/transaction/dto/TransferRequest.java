package com.gabriel.fintransfer.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID payerId,
        @NotNull UUID payeeId,
        @NotNull @Positive BigDecimal amount
) {}
