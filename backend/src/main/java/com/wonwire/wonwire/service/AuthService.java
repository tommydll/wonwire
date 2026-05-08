package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.PasswordResetToken;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.domain.enums.Currency;
import com.wonwire.wonwire.dto.*;
import com.wonwire.wonwire.exception.InvalidTokenException;
import com.wonwire.wonwire.exception.UserAlreadyExistsException;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.PasswordResetTokenRepository;
import com.wonwire.wonwire.repository.UserRepository;
import com.wonwire.wonwire.repository.WalletRepository;
import com.wonwire.wonwire.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    /**
     * Registers a new user, creates an associated wallet with zero balance.
     * Returns a JWT token for immediate authentication after registration.
     */
    @Transactional
    public MessageResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        userRepository.save(user);

        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency(Currency.KRW)
                .build();

        walletRepository.save(wallet);

        return new MessageResponseDTO("Account created successfully. Please sign in.");
    }

    /**
     * Authenticates a user with email and password.
     * Spring Security handles password verification via AuthenticationManager.
     * Returns a JWT token along with user details on successful authentication.
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));
        String token = jwtService.generateToken(user);

        return AuthResponseDTO.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    /**
     * Generates a password reset token and sends a reset email.
     * If the user doesn't exist, we still return a success message to avoid exposing whether an email is registered or not.
     */
    @Transactional
    public MessageResponseDTO forgotPassword(ForgotPasswordRequestDTO request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            // Invalidate any existing token for this user
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });

        return new MessageResponseDTO("If this email is registered, you will receive a reset link shortly.");
    }

    /**
     * Validates the reset token and updates the user's password.
     * Throws an exception if the token is invalid, expired, or already used.
     */
    public MessageResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return new MessageResponseDTO("Password successfully reset. You can now sign in.");
    }
}