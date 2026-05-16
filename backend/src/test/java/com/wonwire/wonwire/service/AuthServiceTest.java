package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.PasswordResetToken;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.dto.*;
import com.wonwire.wonwire.exception.InvalidTokenException;
import com.wonwire.wonwire.exception.UserAlreadyExistsException;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.PasswordResetTokenRepository;
import com.wonwire.wonwire.repository.UserRepository;
import com.wonwire.wonwire.repository.WalletRepository;
import com.wonwire.wonwire.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDTO();
        registerRequest.setEmail("test@wonwire.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@wonwire.com");
        loginRequest.setPassword("password123");

        user = User.builder()
                .email("test@wonwire.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    // -------------------------------------------------------------------------
    // register()
    // -------------------------------------------------------------------------

    @Test
    void register_ShouldCreateUserAndWallet_WhenEmailIsNew() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        MessageResponseDTO response = authService.register(registerRequest);

        // Then
        assertThat(response.getMessage()).isEqualTo("Account created successfully. Please sign in.");
        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void register_ShouldThrowUserAlreadyExistsException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(registerRequest.getEmail());

        verify(userRepository, never()).save(any());
        verify(walletRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // login()
    // -------------------------------------------------------------------------

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt.token.here");

        // When
        AuthResponseDTO response = authService.login(loginRequest);

        // Then
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(response.getLastName()).isEqualTo(user.getLastName());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(loginRequest.getEmail());
    }

    // -------------------------------------------------------------------------
    // forgotPassword()
    // -------------------------------------------------------------------------

    @Test
    void forgotPassword_ShouldSendEmail_WhenUserExists() {
        // Given
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("test@wonwire.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // When
        authService.forgotPassword(request);

        // Then
        verify(passwordResetTokenRepository, times(1)).deleteByUser(user);
        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendPasswordResetEmail(eq(user.getEmail()), anyString());
    }

    @Test
    void forgotPassword_ShouldDoNothing_WhenUserDoesNotExist() {
        // Given
        ForgotPasswordRequestDTO request = new ForgotPasswordRequestDTO();
        request.setEmail("unknown@wonwire.com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When
        authService.forgotPassword(request);

        // Then — no email sent, no token saved
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // -------------------------------------------------------------------------
    // resetPassword()
    // -------------------------------------------------------------------------

    @Test
    void resetPassword_ShouldUpdatePassword_WhenTokenIsValid() {
        // Given
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("valid-token");
        request.setNewPassword("newPassword123");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token("valid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        // When
        authService.resetPassword(request);

        // Then
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        assertThat(resetToken.isUsed()).isTrue();
        verify(userRepository, times(1)).save(user);
        verify(passwordResetTokenRepository, times(1)).save(resetToken);
    }

    @Test
    void resetPassword_ShouldThrowInvalidTokenException_WhenTokenNotFound() {
        // Given
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("invalid-token");
        request.setNewPassword("newPassword123");
        when(passwordResetTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void resetPassword_ShouldThrowInvalidTokenException_WhenTokenIsExpired() {
        // Given
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("expired-token");
        request.setNewPassword("newPassword123");

        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        // When / Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_ShouldThrowInvalidTokenException_WhenTokenAlreadyUsed() {
        // Given
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO();
        request.setToken("used-token");
        request.setNewPassword("newPassword123");

        PasswordResetToken usedToken = PasswordResetToken.builder()
                .token("used-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(true)
                .build();

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        // When / Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("already been used");
    }
}