package com.gabriel.fintransfer.wallet.controller;

import com.gabriel.fintransfer.wallet.dto.DepositRequest;
import com.gabriel.fintransfer.wallet.dto.WalletResponse;
import com.gabriel.fintransfer.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet operations")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get wallet balance by user ID")
    public WalletResponse getByUserId(@PathVariable UUID userId) {
        return WalletResponse.from(walletService.findByUserId(userId));
    }

    @PostMapping("/user/{userId}/deposit")
    @Operation(summary = "Deposit funds into a user's wallet")
    public WalletResponse deposit(@PathVariable UUID userId, @Valid @RequestBody DepositRequest request) {
        return WalletResponse.from(walletService.deposit(userId, request.amount()));
    }
}
