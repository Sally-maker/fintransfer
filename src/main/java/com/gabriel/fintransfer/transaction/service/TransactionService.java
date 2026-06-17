package com.gabriel.fintransfer.transaction.service;

import com.gabriel.fintransfer.transaction.dto.TransactionResponse;
import com.gabriel.fintransfer.transaction.dto.TransferRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request);

    List<TransactionResponse> findByUserId(UUID userId);

    TransactionResponse refund(UUID transactionId, UUID merchantId);
}
