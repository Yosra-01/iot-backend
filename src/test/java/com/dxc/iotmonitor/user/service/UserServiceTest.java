package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.dto.UpdateProfilePictureRequest;
import com.dxc.iotmonitor.user.mapper.UserMapper;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    // ================================================================
    // getProfile tests
    // ================================================================

    @Test
    void getProfile_Success() {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");

        ProfileResponse expectedResponse = new ProfileResponse();
        expectedResponse.setEmail(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        // Act
        ProfileResponse result = userService.getProfile(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
        verify(userMapper, times(1)).toResponse(user);
    }

    // ================================================================
    // updateProfilePicture tests
    // ================================================================

    @Test
    void updateProfilePicture_Success() {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);

        UpdateProfilePictureRequest request = new UpdateProfilePictureRequest();
        request.setProfilePicture("/9j/4AAQSkZJRgABAQ...");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // Act
        userService.updateProfilePicture(email, request);

        // Assert
        assertEquals("/9j/4AAQSkZJRgABAQ...", user.getProfilePicture());
        verify(userRepository, times(1)).save(user);
    }

    // ================================================================
    // updatePassword tests
    // ================================================================

    @Test
    void updatePassword_Success() {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedOldPassword");

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword123!");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "hashedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123!")).thenReturn("hashedNewPassword");

        // Act
        userService.updatePassword(email, request);

        // Assert
        assertEquals("hashedNewPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updatePassword_WrongCurrentPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedOldPassword");

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123!");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedOldPassword")).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.updatePassword(email, request)
        );

        assertEquals("Current password is incorrect.", exception.getMessage());
        verify(userRepository, never()).save(any()); // save should never be called
    }

    // ================================================================
    // deleteUserByEmail tests
    // ================================================================

    @Test
    void deleteUserByEmail_Success() {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUserByEmail(email);

        // Assert
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserByEmail_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String email = "unknown@example.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUserByEmail(email)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any()); // delete should never be called
    }
}