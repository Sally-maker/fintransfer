package com.gabriel.fintransfer.wallet.service;

import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.wallet.domain.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletService {

    Wallet createForUser(User user);

    Wallet findByUserId(UUID userId);

    Wallet findById(UUID walletId);

    Wallet deposit(UUID userId, BigDecimal amount);
}
