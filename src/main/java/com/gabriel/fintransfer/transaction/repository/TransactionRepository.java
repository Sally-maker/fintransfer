package com.gabriel.fintransfer.transaction.repository;

import com.gabriel.fintransfer.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByPayerId(UUID payerId);

    List<Transaction> findByPayeeId(UUID payeeId);
}
