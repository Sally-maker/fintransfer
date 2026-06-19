package com.gabriel.fintransfer.wallet.service;

import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.domain.UserType;
import com.gabriel.fintransfer.wallet.domain.Wallet;
import com.gabriel.fintransfer.wallet.exception.WalletNotFoundException;
import com.gabriel.fintransfer.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void shouldCreateWalletForUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Gabriel")
                .email("gabriel@email.com")
                .userType(UserType.COMMON)
                .build();

        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> {
            Wallet w = inv.getArgument(0);
            return Wallet.builder().id(UUID.randomUUID()).user(w.getUser()).balance(w.getBalance()).build();
        });

        Wallet wallet = walletService.createForUser(user);

        assertThat(wallet.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(wallet.getUser()).isEqualTo(user);
    }

    @Test
    void shouldDepositFunds() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(userId).build())
                .balance(new BigDecimal("100.00"))
                .build();

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));

        Wallet result = walletService.deposit(userId, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.findByUserId(userId))
                .isInstanceOf(WalletNotFoundException.class);
    }
}
