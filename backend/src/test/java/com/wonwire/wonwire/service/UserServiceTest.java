package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.ChangePasswordRequestDTO;
import com.wonwire.wonwire.dto.MessageResponseDTO;
import com.wonwire.wonwire.dto.UpdateProfileRequestDTO;
import com.wonwire.wonwire.dto.UserProfileResponseDTO;
import com.wonwire.wonwire.exception.UserAlreadyExistsException;
import com.wonwire.wonwire.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UpdateProfileRequestDTO updateRequest;
    private ChangePasswordRequestDTO changePasswordRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("alice@wonwire.com")
                .password("encodedCurrentPassword")
                .firstName("Alice")
                .lastName("Smith")
                .build();

        updateRequest = new UpdateProfileRequestDTO();
        updateRequest.setFirstName("Alicia");
        updateRequest.setLastName("Smith");
        updateRequest.setEmail("alice@wonwire.com");

        changePasswordRequest = new ChangePasswordRequestDTO();
        changePasswordRequest.setCurrentPassword("currentPassword123");
        changePasswordRequest.setNewPassword("newPassword123");
    }

    // -------------------------------------------------------------------------
    // getProfile()
    // -------------------------------------------------------------------------

    @Test
    void getProfile_ShouldReturnUserInfo() {
        // When
        UserProfileResponseDTO response = userService.getProfile(user);

        // Then
        assertThat(response.getFirstName()).isEqualTo("Alice");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getEmail()).isEqualTo("alice@wonwire.com");
    }

    // -------------------------------------------------------------------------
    // updateProfile()
    // -------------------------------------------------------------------------

    @Test
    void updateProfile_ShouldUpdateFields_WhenRequestIsValid() {
        // Given
        updateRequest.setFirstName("Alicia");
        updateRequest.setLastName("Johnson");
        updateRequest.setEmail("alice@wonwire.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserProfileResponseDTO response = userService.updateProfile(user, updateRequest);

        // Then
        assertThat(response.getFirstName()).isEqualTo("Alicia");
        assertThat(response.getLastName()).isEqualTo("Johnson");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateProfile_ShouldThrowUserAlreadyExistsException_WhenNewEmailAlreadyTaken() {
        // Given — user tries to change to an email that belongs to someone else
        updateRequest.setEmail("bob@wonwire.com");
        when(userRepository.existsByEmail("bob@wonwire.com")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> userService.updateProfile(user, updateRequest))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_ShouldNotCheckUniqueness_WhenEmailIsUnchanged() {
        // Given — same email, no uniqueness check needed
        updateRequest.setEmail("alice@wonwire.com");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.updateProfile(user, updateRequest);

        // Then — existsByEmail should never be called
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(user);
    }

    // -------------------------------------------------------------------------
    // changePassword()
    // -------------------------------------------------------------------------

    @Test
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrect() {
        // Given
        when(passwordEncoder.matches("currentPassword123", "encodedCurrentPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        MessageResponseDTO response = userService.changePassword(user, changePasswordRequest);

        // Then
        assertThat(response.getMessage()).isEqualTo("Password successfully updated");
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changePassword_ShouldThrowBadCredentialsException_WhenCurrentPasswordIsWrong() {
        // Given
        when(passwordEncoder.matches("currentPassword123", "encodedCurrentPassword")).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> userService.changePassword(user, changePasswordRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }
}