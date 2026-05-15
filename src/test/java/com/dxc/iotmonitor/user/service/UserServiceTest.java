package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.config.ProfilePictureProperties;
import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.mapper.UserMapper;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Mock
    private ProfilePictureProperties profilePictureProperties;

    @InjectMocks
    private UserService userService;

    @TempDir
    Path tempProfilePicturesRoot;

    @BeforeEach
    void bindProfilePictureRoot() {
        lenient().when(profilePictureProperties.resolvedRoot()).thenReturn(tempProfilePicturesRoot);
    }

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
        assertEquals("/api/user/profile/picture", result.getProfilePicture());
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
        verify(userMapper, times(1)).toResponse(user);
    }

    // ================================================================
    // updateProfilePicture tests
    // ================================================================

    @Test
    void updateProfilePicture_Success() throws IOException {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());

        // MockMultipartFile simulates a real uploaded file without touching the real filesystem
        MockMultipartFile file = new MockMultipartFile(
                "file",                      // form field name
                "profile.jpg",               // original filename
                "image/jpeg",                // content type — must start with "image/"
                "fake-image-bytes".getBytes() // fake content — we just need bytes, not a real image
        );

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // Act
        userService.updateProfilePicture(email, file);

        // Assert — file on disk only; path is not persisted on User
        Path expectedFile = tempProfilePicturesRoot.resolve(user.getUserId() + ".jpeg");
        assertTrue(Files.isRegularFile(expectedFile));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfilePicture_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String email = "unknown@example.com";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes()
        );

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateProfilePicture(email, file)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfilePicture_InvalidFileType_ThrowsIllegalArgumentException() throws IOException {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());

        // This file has a PDF content type — should be rejected
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake-pdf-bytes".getBytes()
        );

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfilePicture(email, file)
        );

        assertEquals("Only image files are allowed.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ================================================================
    // getProfilePicture tests
    // ================================================================

    @Test
    void getProfilePicture_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String email = "unknown@example.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfilePicture(email)
        );
    }

    @Test
    void getProfilePicture_NoPictureSet_ThrowsResourceNotFoundException() {
        // Arrange
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        // Act & Assert — no matching file on disk
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfilePicture(email)
        );

        assertEquals("No profile picture found", exception.getMessage());
    }

    // ================================================================
    // updatePassword tests
    // ================================================================

    @Test
    void updatePassword_Success() {
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

        userService.updatePassword(email, request);

        assertEquals("hashedNewPassword", user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updatePassword_WrongCurrentPassword_ThrowsInvalidCredentialsException() {
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedOldPassword");

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword123!");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedOldPassword")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.updatePassword(email, request)
        );

        assertEquals("Current password is incorrect.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ================================================================
    // deleteUserByEmail tests
    // ================================================================

    @Test
    void deleteUserByEmail_Success() throws IOException {
        String email = "john.doe@example.com";

        User user = new User();
        user.setEmail(email);
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);

        Path picture = tempProfilePicturesRoot.resolve(userId + ".jpeg");
        Files.createDirectories(tempProfilePicturesRoot);
        Files.writeString(picture, "x");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        userService.deleteUserByEmail(email);

        assertFalse(Files.exists(picture));
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserByEmail_UserNotFound_ThrowsResourceNotFoundException() {
        String email = "unknown@example.com";

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUserByEmail(email)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }
}