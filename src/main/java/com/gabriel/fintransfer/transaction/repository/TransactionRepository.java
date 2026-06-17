package com.gabriel.fintransfer.transaction.repository;

import com.gabriel.fintransfer.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByPayerId(UUID payerId);

    List<Transaction> findByPayeeId(UUID payeeId);

    @Query("SELECT t FROM Transaction t WHERE t.payer.id = :walletId OR t.payee.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findByWalletId(UUID walletId);
}
