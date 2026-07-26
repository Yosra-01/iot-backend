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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProfilePictureStorageService profilePictureStorageService;
    private final AlertRepository alertRepository;
    private final SettingsRepository settingsRepository;
    private final PollingIntervalRepository pollingIntervalRepository;

    public ProfileResponse getProfile(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    public String updateProfilePicture(String email, MultipartFile file) throws IOException {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String previousProfilePicture = user.getProfilePicture();
        String newProfilePicture = profilePictureStorageService.upload(user.getUserId(), file);
        if (previousProfilePicture != null) {
            try {
                profilePictureStorageService.deleteByPublicUrl(previousProfilePicture);
            } catch (RuntimeException e) {
                cleanupUploadedReplacement(newProfilePicture);
                throw e;
            }
        }

        user.setProfilePicture(newProfilePicture);
        userRepository.save(user);
        return newProfilePicture;
    }

    public URI getProfilePictureUri(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getProfilePicture() == null) {
            throw new ResourceNotFoundException("No profile picture found");
        }

        return toPublicImageUri(user.getProfilePicture());
    }

    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    @Transactional
    public void deleteUserByEmail(String email) {
        String normalized = email == null ? null : email.trim();
        User user = userRepository
                .findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getProfilePicture() != null) {
            profilePictureStorageService.deleteByPublicUrl(user.getProfilePicture());
        }
        pollingIntervalRepository.deleteByUser(user);
        settingsRepository.deleteByUser(user);
        alertRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private void cleanupUploadedReplacement(String newProfilePicture) {
        try {
            profilePictureStorageService.deleteByPublicUrl(newProfilePicture);
        } catch (RuntimeException ignored) {
            // Preserve the original replacement failure.
        }
    }

    private URI toPublicImageUri(String profilePicture) {
        try {
            URI uri = URI.create(profilePicture.trim());
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                return uri;
            }
        } catch (IllegalArgumentException ignored) {
            // Treat malformed legacy values as no public profile picture.
        }
        throw new ResourceNotFoundException("No profile picture found");
    }
}
