package com.gabriel.fintransfer.user.service;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.domain.UserType;
import com.gabriel.fintransfer.user.dto.CreateUserRequest;
import com.gabriel.fintransfer.user.dto.UserResponse;
import com.gabriel.fintransfer.user.exception.UserNotFoundException;
import com.gabriel.fintransfer.user.repository.UserRepository;
import com.gabriel.fintransfer.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest(
                "Gabriel", "12345678901", "gabriel@email.com", "senha123", UserType.COMMON
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByCpfCnpj(request.cpfCnpj())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return User.builder()
                    .id(UUID.randomUUID())
                    .name(u.getName())
                    .email(u.getEmail())
                    .cpfCnpj(u.getCpfCnpj())
                    .password(u.getPassword())
                    .userType(u.getUserType())
                    .build();
        });

        UserResponse response = userService.create(request);

        assertThat(response.name()).isEqualTo("Gabriel");
        assertThat(response.email()).isEqualTo("gabriel@email.com");
        assertThat(response.userType()).isEqualTo(UserType.COMMON);
        verify(walletService).createForUser(any(User.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest(
                "Gabriel", "12345678901", "gabriel@email.com", "senha123", UserType.COMMON
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void shouldRejectDuplicateCpfCnpj() {
        CreateUserRequest request = new CreateUserRequest(
                "Gabriel", "12345678901", "gabriel@email.com", "senha123", UserType.COMMON
        );
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByCpfCnpj(request.cpfCnpj())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF/CNPJ already registered");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UserNotFoundException.class);
    }
}
