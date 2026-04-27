package com.wonwire.wonwire.repository;

import com.wonwire.wonwire.domain.Transaction;
import com.wonwire.wonwire.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds all transactions where the user is either sender or receiver.
     * Paginated to avoid loading the entire transaction history at once.
     */
    Page<Transaction> findBySenderOrReceiver(User sender, User receiver, Pageable pageable);

    /**
     * Before executing a transfer, checks if a transaction with the given idempotency key already exists.
     * Prevents the same transfer from being executed twice.
     * (The frontend generates a unique UUID per filled transfer form and send it in the header)
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}