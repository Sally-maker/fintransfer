package com.gabriel.fintransfer.transaction.service;

import com.gabriel.fintransfer.notification.dto.NotificationRequest;
import com.gabriel.fintransfer.notification.service.NotificationService;
import com.gabriel.fintransfer.transaction.domain.Transaction;
import com.gabriel.fintransfer.transaction.domain.TransactionStatus;
import com.gabriel.fintransfer.transaction.repository.TransactionRepository;
import com.gabriel.fintransfer.wallet.domain.Wallet;
import com.gabriel.fintransfer.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionExecutor {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Transactional
    public Transaction execute(UUID payerWalletId, UUID payeeWalletId, BigDecimal amount, String authReason) {
        Wallet payer = walletRepository.findById(payerWalletId).orElseThrow();
        Wallet payee = walletRepository.findById(payeeWalletId).orElseThrow();

        payer.debit(amount);
        payee.credit(amount);

        walletRepository.save(payer);
        walletRepository.save(payee);

        Transaction transaction = Transaction.builder()
                .payer(payer)
                .payee(payee)
                .amount(amount)
                .status(TransactionStatus.COMPLETED)
                .authorizationReason(authReason)
                .build();

        return transactionRepository.save(transaction);
    }

    public void notifyPayee(String payeeEmail, BigDecimal amount, String payerName) {
        notificationService.notify(new NotificationRequest(payeeEmail, amount, payerName));
    }
}
