package com.gabriel.fintransfer.transaction.service;

import com.gabriel.fintransfer.transaction.dto.TransactionResponse;
import com.gabriel.fintransfer.transaction.dto.TransferRequest;

public interface TransactionService {

    TransactionResponse transfer(TransferRequest request);
}
