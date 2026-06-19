package com.gabriel.fintransfer.transaction.service;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionAuthorizationService authorizationService;

    @Mock
    private TransactionExecutor transactionExecutor;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User createUser(UUID id, String name, UserType type) {
        return User.builder().id(id).name(name).email(name + "@email.com").userType(type).build();
    }

    private Wallet createWallet(UUID id, User user, BigDecimal balance) {
        return Wallet.builder().id(id).user(user).balance(balance).build();
    }

    @Test
    void shouldTransferSuccessfully() {
        UUID payerId = UUID.randomUUID();
        UUID payeeId = UUID.randomUUID();
        UUID payerWalletId = UUID.randomUUID();
        UUID payeeWalletId = UUID.randomUUID();

        User payer = createUser(payerId, "Payer", UserType.COMMON);
        User payee = createUser(payeeId, "Payee", UserType.COMMON);
        Wallet payerWallet = createWallet(payerWalletId, payer, new BigDecimal("1000.00"));
        Wallet payeeWallet = createWallet(payeeWalletId, payee, new BigDecimal("500.00"));

        TransferRequest request = new TransferRequest(payerId, payeeId, new BigDecimal("100.00"));

        when(userService.findEntityById(payerId)).thenReturn(payer);
        when(userService.findEntityById(payeeId)).thenReturn(payee);
        when(walletService.findByUserId(payerId)).thenReturn(payerWallet);
        when(walletService.findByUserId(payeeId)).thenReturn(payeeWallet);
        when(authorizationService.authorize(any(TransactionContext.class)))
                .thenReturn(new AuthorizationResult(true, "Approved"));

        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .payer(payerWallet)
                .payee(payeeWallet)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .build();
        when(transactionExecutor.execute(payerWalletId, payeeWalletId, new BigDecimal("100.00"), "Approved"))
                .thenReturn(tx);

        TransactionResponse response = transactionService.transfer(request);

        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
        verify(transactionExecutor).notifyPayee(payee.getEmail(), new BigDecimal("100.00"), payer.getName());
    }

    @Test
    void shouldRejectMerchantAsPayer() {
        UUID payerId = UUID.randomUUID();
        UUID payeeId = UUID.randomUUID();

        User merchant = createUser(payerId, "Loja", UserType.MERCHANT);
        User payee = createUser(payeeId, "Payee", UserType.COMMON);
        Wallet merchantWallet = createWallet(UUID.randomUUID(), merchant, new BigDecimal("1000.00"));

        when(userService.findEntityById(payerId)).thenReturn(merchant);
        when(userService.findEntityById(payeeId)).thenReturn(payee);
        when(walletService.findByUserId(payerId)).thenReturn(merchantWallet);
        when(walletService.findByUserId(payeeId)).thenReturn(createWallet(UUID.randomUUID(), payee, BigDecimal.ZERO));

        TransferRequest request = new TransferRequest(payerId, payeeId, new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(UnauthorizedTransactionException.class)
                .hasMessageContaining("Merchants cannot initiate transfers");
    }

    @Test
    void shouldRejectInsufficientBalance() {
        UUID payerId = UUID.randomUUID();
        UUID payeeId = UUID.randomUUID();

        User payer = createUser(payerId, "Payer", UserType.COMMON);
        User payee = createUser(payeeId, "Payee", UserType.COMMON);
        Wallet payerWallet = createWallet(UUID.randomUUID(), payer, new BigDecimal("50.00"));

        when(userService.findEntityById(payerId)).thenReturn(payer);
        when(userService.findEntityById(payeeId)).thenReturn(payee);
        when(walletService.findByUserId(payerId)).thenReturn(payerWallet);
        when(walletService.findByUserId(payeeId)).thenReturn(createWallet(UUID.randomUUID(), payee, BigDecimal.ZERO));

        TransferRequest request = new TransferRequest(payerId, payeeId, new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void shouldRejectUnauthorizedTransaction() {
        UUID payerId = UUID.randomUUID();
        UUID payeeId = UUID.randomUUID();

        User payer = createUser(payerId, "Payer", UserType.COMMON);
        User payee = createUser(payeeId, "Payee", UserType.COMMON);
        Wallet payerWallet = createWallet(UUID.randomUUID(), payer, new BigDecimal("1000.00"));

        when(userService.findEntityById(payerId)).thenReturn(payer);
        when(userService.findEntityById(payeeId)).thenReturn(payee);
        when(walletService.findByUserId(payerId)).thenReturn(payerWallet);
        when(walletService.findByUserId(payeeId)).thenReturn(createWallet(UUID.randomUUID(), payee, BigDecimal.ZERO));
        when(authorizationService.authorize(any(TransactionContext.class)))
                .thenReturn(new AuthorizationResult(false, "Suspicious activity"));

        TransferRequest request = new TransferRequest(payerId, payeeId, new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(UnauthorizedTransactionException.class)
                .hasMessageContaining("Suspicious activity");
    }
}
