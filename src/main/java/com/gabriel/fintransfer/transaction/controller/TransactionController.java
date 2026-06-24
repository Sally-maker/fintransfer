package com.gabriel.fintransfer.transaction.controller;

import com.gabriel.fintransfer.transaction.dto.TransactionResponse;
import com.gabriel.fintransfer.transaction.dto.TransferRequest;
import com.gabriel.fintransfer.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transfer operations")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer funds between users")
    public TransactionResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transactionService.transfer(request);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get transaction history for a user")
    public List<TransactionResponse> findByUserId(@PathVariable UUID userId) {
        return transactionService.findByUserId(userId);
    }

    @PostMapping("/{transactionId}/refund")
    @Operation(summary = "Request a refund for a transaction (payer only, analyzed by AI)")
    public TransactionResponse refund(@PathVariable UUID transactionId, @Valid @RequestBody com.gabriel.fintransfer.transaction.dto.RefundRequest request) {
        return transactionService.refund(transactionId, request);
    }
}
