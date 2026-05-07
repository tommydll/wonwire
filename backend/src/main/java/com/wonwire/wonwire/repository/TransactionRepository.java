package com.wonwire.wonwire.repository;

import com.wonwire.wonwire.domain.Transaction;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    /**
     * Returns all unique receivers when the user was the sender.
     * Used to build the contacts list.
     */
    @Query("SELECT DISTINCT t.receiver FROM Transaction t WHERE t.sender = :user AND t.type = :type")
    List<User> findReceiversBySender(@Param("user") User user, @Param("type") TransactionType type);

    /**
     * Returns all unique senders when the user was the receiver.
     * Used to build the contacts list.
     */
    @Query("SELECT DISTINCT t.sender FROM Transaction t WHERE t.receiver = :user AND t.type = :type")
    List<User> findSendersByReceiver(@Param("user") User user, @Param("type") TransactionType type);
}