package com.gabriel.fintransfer.transaction.service;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import com.gabriel.fintransfer.transaction.authorization.AuthorizationResult;
import com.gabriel.fintransfer.transaction.authorization.TransactionAuthorizationService;
import com.gabriel.fintransfer.transaction.authorization.RefundContext;
import com.gabriel.fintransfer.transaction.authorization.TransactionContext;
import com.gabriel.fintransfer.transaction.domain.Transaction;
import com.gabriel.fintransfer.transaction.domain.TransactionStatus;
import com.gabriel.fintransfer.transaction.dto.RefundRequest;
import com.gabriel.fintransfer.transaction.dto.TransactionResponse;
import com.gabriel.fintransfer.transaction.dto.TransferRequest;
import com.gabriel.fintransfer.transaction.exception.UnauthorizedTransactionException;
import com.gabriel.fintransfer.transaction.repository.TransactionRepository;
import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.domain.UserType;
import com.gabriel.fintransfer.user.service.UserService;
import com.gabriel.fintransfer.wallet.domain.Wallet;
import com.gabriel.fintransfer.wallet.exception.InsufficientBalanceException;
import com.gabriel.fintransfer.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionAuthorizationService authorizationService;
    private final TransactionExecutor transactionExecutor;
    private final TransactionRepository transactionRepository;

    @Override
    public TransactionResponse transfer(TransferRequest request) {
        User payer = userService.findEntityById(request.payerId());
        User payee = userService.findEntityById(request.payeeId());
        Wallet payerWallet = walletService.findByUserId(payer.getId());
        Wallet payeeWallet = walletService.findByUserId(payee.getId());

        validate(payer, payerWallet, request);

        AuthorizationResult auth = authorizationService.authorize(
                TransactionContext.of(payer, payee, payerWallet.getBalance(), request.amount())
        );
        if (!auth.approved()) {
            saveRejected(payerWallet, payeeWallet, request.amount(), auth.reason());
            throw new UnauthorizedTransactionException(auth.reason());
        }

        Transaction tx = transactionExecutor.execute(
                payerWallet.getId(), payeeWallet.getId(), request.amount(), auth.reason()
        );

        transactionExecutor.notifyPayee(payee.getEmail(), request.amount(), payer.getName());

        return TransactionResponse.from(tx);
    }

    @Override
    public List<TransactionResponse> findByUserId(UUID userId) {
        Wallet wallet = walletService.findByUserId(userId);
        return transactionRepository.findByWalletId(wallet.getId()).stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TransactionResponse refund(UUID transactionId, RefundRequest request) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found", HttpStatus.NOT_FOUND));

        if (tx.getStatus() != TransactionStatus.COMPLETED) {
            throw new BusinessException("Only completed transactions can be refunded", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User requester = userService.findEntityById(request.requesterId());
        Wallet payerWallet = walletService.findById(tx.getPayer().getId());

        if (!payerWallet.getUser().getId().equals(request.requesterId())) {
            throw new BusinessException("Only the payer can request a refund", HttpStatus.FORBIDDEN);
        }

        Wallet payeeWallet = walletService.findById(tx.getPayee().getId());
        User recipient = payeeWallet.getUser();

        AuthorizationResult auth = authorizationService.authorizeRefund(
                RefundContext.of(tx, requester.getName(), recipient.getName(), request.reason())
        );

        if (!auth.approved()) {
            throw new BusinessException("Refund denied: " + auth.reason(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        transactionExecutor.execute(
                payeeWallet.getId(), payerWallet.getId(), tx.getAmount(), "Refund: " + auth.reason()
        );
        tx.reverse();
        return TransactionResponse.from(transactionRepository.save(tx));
    }

    private void validate(User payer, Wallet payerWallet, TransferRequest request) {
        if (payer.getUserType() == UserType.MERCHANT) {
            throw new UnauthorizedTransactionException("Merchants cannot initiate transfers");
        }
        if (payerWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private void saveRejected(Wallet payer, Wallet payee, java.math.BigDecimal amount, String reason) {
        Transaction rejected = Transaction.builder()
                .payer(payer)
                .payee(payee)
                .payerUser(payer.getUser())
                .payeeUser(payee.getUser())
                .amount(amount)
                .status(TransactionStatus.REJECTED)
                .authorizationReason(reason)
                .build();
        transactionRepository.save(rejected);
    }
}
