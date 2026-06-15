package com.gabriel.fintransfer.transaction.service;

import com.gabriel.fintransfer.transaction.authorization.AuthorizationResult;
import com.gabriel.fintransfer.transaction.authorization.TransactionAuthorizationService;
import com.gabriel.fintransfer.transaction.authorization.TransactionContext;
import com.gabriel.fintransfer.transaction.domain.Transaction;
import com.gabriel.fintransfer.transaction.dto.TransactionResponse;
import com.gabriel.fintransfer.transaction.dto.TransferRequest;
import com.gabriel.fintransfer.transaction.exception.UnauthorizedTransactionException;
import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.domain.UserType;
import com.gabriel.fintransfer.user.service.UserService;
import com.gabriel.fintransfer.wallet.domain.Wallet;
import com.gabriel.fintransfer.wallet.exception.InsufficientBalanceException;
import com.gabriel.fintransfer.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionAuthorizationService authorizationService;
    private final TransactionExecutor transactionExecutor;

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

    private void validate(User payer, Wallet payerWallet, TransferRequest request) {
        if (payer.getUserType() == UserType.MERCHANT) {
            throw new UnauthorizedTransactionException("Merchants cannot initiate transfers");
        }
        if (payerWallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }
    }
}
