package com.wonwire.wonwire.repository;

import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Finds a wallet by its owner.
     * Used to retrieve balance and perform transfers.
     */
    Optional<Wallet> findByUser(User user);

    /**
     * Finds a wallet by its owner with a pessimistic write lock.
     * Used during transfers to prevent concurrent balance modifications.
     * Double protection with the optimistic lock from the Wallet version.
     * We lock it directly, no conflict possible.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findWithLockByUser(User user);
}