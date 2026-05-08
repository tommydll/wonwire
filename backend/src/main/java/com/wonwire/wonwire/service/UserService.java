package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.ChangePasswordRequestDTO;
import com.wonwire.wonwire.dto.MessageResponseDTO;
import com.wonwire.wonwire.dto.UpdateProfileRequestDTO;
import com.wonwire.wonwire.dto.UserProfileResponseDTO;
import com.wonwire.wonwire.exception.UserAlreadyExistsException;
import com.wonwire.wonwire.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns the authenticated user's profile information.
     */
    public UserProfileResponseDTO getProfile(User user) {
        return UserProfileResponseDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    /**
     * Updates the authenticated user's profile (firstName, lastName, email).
     * Checks email uniqueness if the email has changed.
     */
    @Transactional
    public UserProfileResponseDTO updateProfile(User user, UpdateProfileRequestDTO request) {
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return UserProfileResponseDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    /**
     * Changes the authenticated user's password.
     * Verifies the current password before updating.
     */
    @Transactional
    public MessageResponseDTO changePassword(User user, ChangePasswordRequestDTO request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new MessageResponseDTO("Password successfully updated");
    }
}