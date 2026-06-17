package com.gabriel.fintransfer.wallet.dto;

import com.gabriel.fintransfer.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(UUID id, UUID userId, BigDecimal balance) {

    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(wallet.getId(), wallet.getUser().getId(), wallet.getBalance());
    }
}
