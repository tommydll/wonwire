package com.wonwire.wonwire.repository;

import com.wonwire.wonwire.domain.PasswordResetToken;
import com.wonwire.wonwire.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}