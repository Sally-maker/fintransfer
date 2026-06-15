package com.gabriel.fintransfer.user.repository;

import com.gabriel.fintransfer.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByCpfCnpj(String cpfCnpj);

    Optional<User> findByEmail(String email);
}
