package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.polling.PollingIntervalRepository;
import com.dxc.iotmonitor.settings.repository.SettingsRepository;
import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.mapper.UserMapper;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProfilePictureStorageService profilePictureStorageService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private SettingsRepository settingsRepository;

    @Mock
    private PollingIntervalRepository pollingIntervalRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getProfile_noPicture_returnsNullProfilePicture() {
        String email = "john.doe@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());

        ProfileResponse expectedResponse = new ProfileResponse();
        expectedResponse.setEmail(email);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        ProfileResponse result = userService.getProfile(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNull(result.getProfilePicture());
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void getProfile_withCdnUrl_returnsStoredProfilePicture() {
        String email = "john.doe@example.com";
        String storedUrl = "https://cdn.example.com/profile-pictures/user/pic.jpeg";
        User user = new User();
        user.setEmail(email);
        user.setProfilePicture(storedUrl);

        ProfileResponse expectedResponse = new ProfileResponse();
        expectedResponse.setEmail(email);
        expectedResponse.setProfilePicture(storedUrl);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        ProfileResponse result = userService.getProfile(email);

        assertEquals(storedUrl, result.getProfilePicture());
    }

    @Test
    void updateProfilePicture_success_uploadsAndStoresCdnUrl() throws IOException {
        String email = "john.doe@example.com";
        String cdnUrl = "https://cdn.example.com/profile-pictures/user/new.jpeg";
        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());
        MockMultipartFile file = imageFile();

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(profilePictureStorageService.upload(user.getUserId(), file)).thenReturn(cdnUrl);

        String result = userService.updateProfilePicture(email, file);

        assertEquals(cdnUrl, result);
        assertEquals(cdnUrl, user.getProfilePicture());
        verify(userRepository, times(1)).save(user);
        verify(profilePictureStorageService, never()).deleteByPublicUrl(any());
    }

    @Test
    void updateProfilePicture_replacement_deletesOldObject() throws IOException {
        String email = "john.doe@example.com";
        String oldUrl = "https://cdn.example.com/profile-pictures/user/old.jpeg";
        String newUrl = "https://cdn.example.com/profile-pictures/user/new.jpeg";
        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());
        user.setProfilePicture(oldUrl);
        MockMultipartFile file = imageFile();

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(profilePictureStorageService.upload(user.getUserId(), file)).thenReturn(newUrl);

        userService.updateProfilePicture(email, file);

        verify(profilePictureStorageService).deleteByPublicUrl(oldUrl);
        assertEquals(newUrl, user.getProfilePicture());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfilePicture_userNotFound_throwsResourceNotFoundException() throws IOException {
        String email = "unknown@example.com";
        MockMultipartFile file = imageFile();

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateProfilePicture(email, file));

        assertEquals("User not found", exception.getMessage());
        verify(profilePictureStorageService, never()).upload(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
void updateProfilePictureStorageValidationFailureDoesNotSave() throws IOException {
    String email = "john.doe@example.com";
    User user = new User();
    user.setEmail(email);
    user.setUserId(UUID.randomUUID());
    MockMultipartFile file = imageFile();

    when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
    when(profilePictureStorageService.upload(user.getUserId(), file))
            .thenThrow(new IllegalArgumentException("Only image files are allowed."));

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> userService.updateProfilePicture(email, file));

    assertEquals("Only image files are allowed.", exception.getMessage());
    verify(userRepository, never()).save(any());
}

    @Test
    void getProfilePictureUri_userNotFound_throwsResourceNotFoundException() {
        String email = "unknown@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfilePictureUri(email));
    }

    @Test
    void getProfilePictureUri_noPictureSet_throwsResourceNotFoundException() {
        String email = "john.doe@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfilePictureUri(email));

        assertEquals("No profile picture found", exception.getMessage());
    }

    @Test
    void getProfilePictureUri_legacyLocalPath_throwsResourceNotFoundException() {
        String email = "john.doe@example.com";
        User user = new User();
        user.setEmail(email);
        user.setProfilePicture("uploads/profile-pictures/user.jpeg");

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getProfilePictureUri(email));

        assertEquals("No profile picture found", exception.getMessage());
    }

    @Test
    void getProfilePictureUri_withPicture_returnsStoredUri() {
        String email = "john.doe@example.com";
        String cdnUrl = "https://cdn.example.com/profile-pictures/user/pic.jpeg";
        User user = new User();
        user.setEmail(email);
        user.setProfilePicture(cdnUrl);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        URI result = userService.getProfilePictureUri(email);

        assertEquals(URI.create(cdnUrl), result);
    }

    @Test
    void updatePassword_success() {
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
    void updatePassword_wrongCurrentPassword_throwsInvalidCredentialsException() {
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
                () -> userService.updatePassword(email, request));

        assertEquals("Current password is incorrect.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserByEmail_success_deletesProfilePictureObjectAndUserData() {
        String email = "john.doe@example.com";
        String cdnUrl = "https://cdn.example.com/profile-pictures/user/pic.jpeg";
        User user = new User();
        user.setEmail(email);
        user.setUserId(UUID.randomUUID());
        user.setProfilePicture(cdnUrl);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));

        userService.deleteUserByEmail(email);

        verify(profilePictureStorageService).deleteByPublicUrl(cdnUrl);
        verify(pollingIntervalRepository).deleteByUser(user);
        verify(settingsRepository).deleteByUser(user);
        verify(alertRepository).deleteByUser(user);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUserByEmail_pictureDeleteFails_doesNotDeleteUser() {
        String email = "john.doe@example.com";
        String cdnUrl = "https://cdn.example.com/profile-pictures/user/pic.jpeg";
        User user = new User();
        user.setEmail(email);
        user.setProfilePicture(cdnUrl);

        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        org.mockito.Mockito.doThrow(new IllegalStateException("Failed to delete profile picture from R2"))
                .when(profilePictureStorageService)
                .deleteByPublicUrl(cdnUrl);

        assertThrows(IllegalStateException.class, () -> userService.deleteUserByEmail(email));

        verify(userRepository, never()).delete(any());
        verify(alertRepository, never()).deleteByUser(any());
        verify(settingsRepository, never()).deleteByUser(any());
        verify(pollingIntervalRepository, never()).deleteByUser(any());
    }

    @Test
    void deleteUserByEmail_userNotFound_throwsResourceNotFoundException() {
        String email = "unknown@example.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUserByEmail(email));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }

    private MockMultipartFile imageFile() {
        return new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes());
    }
}
