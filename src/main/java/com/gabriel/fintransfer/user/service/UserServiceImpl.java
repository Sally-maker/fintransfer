package com.gabriel.fintransfer.user.service;

import com.gabriel.fintransfer.shared.exception.BusinessException;
import com.gabriel.fintransfer.user.domain.User;
import com.gabriel.fintransfer.user.dto.CreateUserRequest;
import com.gabriel.fintransfer.user.dto.UserResponse;
import com.gabriel.fintransfer.user.exception.UserNotFoundException;
import com.gabriel.fintransfer.user.repository.UserRepository;
import com.gabriel.fintransfer.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByCpfCnpj(request.cpfCnpj())) {
            throw new BusinessException("CPF/CNPJ already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .name(request.name())
                .cpfCnpj(request.cpfCnpj())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .userType(request.userType())
                .build();

        User saved = userRepository.save(user);
        walletService.createForUser(saved);
        return UserResponse.from(saved);
    }

    @Override
    public UserResponse findById(UUID id) {
        return UserResponse.from(findEntityById(id));
    }

    @Override
    public User findEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }
}
