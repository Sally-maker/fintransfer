package com.gabriel.fintransfer.transaction.service;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import com.gabriel.fintransfer.transaction.authorization.AuthorizationResult;
import com.gabriel.fintransfer.transaction.authorization.TransactionAuthorizationService;
import com.gabriel.fintransfer.transaction.authorization.TransactionContext;
import com.gabriel.fintransfer.transaction.domain.Transaction;
import com.gabriel.fintransfer.transaction.domain.TransactionStatus;
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
    public TransactionResponse refund(UUID transactionId, UUID merchantId) {
        User merchant = userService.findEntityById(merchantId);
        if (merchant.getUserType() != UserType.MERCHANT) {
            throw new BusinessException("Only merchants can issue refunds", HttpStatus.FORBIDDEN);
        }

        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transaction not found", HttpStatus.NOT_FOUND));

        if (tx.getStatus() != TransactionStatus.COMPLETED) {
            throw new BusinessException("Only completed transactions can be refunded", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Wallet payeeWallet = walletService.findById(tx.getPayee().getId());
        if (payeeWallet.getUser().getId().equals(merchantId)) {
            transactionExecutor.execute(
                    payeeWallet.getId(), tx.getPayer().getId(), tx.getAmount(), "Refund by merchant"
            );
            tx.reverse();
            return TransactionResponse.from(transactionRepository.save(tx));
        }

        throw new BusinessException("Merchant can only refund transactions they received", HttpStatus.FORBIDDEN);
    }

    private void validate(User payer, Wallet payerWallet, TransferRequest request) {
        if (payer.getUserType() == UserType.MERCHANT) {
            throw new UnauthorizedTransactionException("Merchants cannot initiate transfers");
        }
        if (payerWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }
    }
}
