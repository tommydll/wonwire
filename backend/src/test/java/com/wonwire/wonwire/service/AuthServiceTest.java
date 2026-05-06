package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.dto.AuthResponseDTO;
import com.wonwire.wonwire.dto.LoginRequestDTO;
import com.wonwire.wonwire.dto.MessageResponseDTO;
import com.wonwire.wonwire.dto.RegisterRequestDTO;
import com.wonwire.wonwire.exception.UserAlreadyExistsException;
import com.wonwire.wonwire.exception.UserNotFoundException;
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
        registerRequest.setFullName("Test Wonwire");

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@wonwire.com");
        loginRequest.setPassword("password123");

        user = User.builder()
                .email("test@wonwire.com")
                .password("encodedPassword")
                .fullName("Test Wonwire")
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
        assertThat(response.getFullName()).isEqualTo(user.getFullName());
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
}