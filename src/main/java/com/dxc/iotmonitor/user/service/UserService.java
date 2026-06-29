package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.alert.repository.AlertRepository;
import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.polling.PollingIntervalRepository;
import com.dxc.iotmonitor.settings.repository.SettingsRepository;
import com.dxc.iotmonitor.user.config.ProfilePictureProperties;
import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.mapper.UserMapper;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProfilePictureProperties profilePictureProperties;
    private final AlertRepository alertRepository;
    private final SettingsRepository settingsRepository;
    private final PollingIntervalRepository pollingIntervalRepository;

    public ProfileResponse getProfile(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        ProfileResponse response = userMapper.toResponse(user);
        response.setProfilePicture(user.getProfilePicture());
        return response;
    }

    public void updateProfilePicture(String email, MultipartFile file) throws IOException {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String extension = imageFileExtension(file.getContentType());
        Path root = profilePictureProperties.resolvedRoot();
        Files.createDirectories(root);

        if (user.getProfilePicture() != null) {
            Path oldFile = Paths.get(user.getProfilePicture());
            Files.deleteIfExists(oldFile);
        }

        Path dest = root.resolve(user.getUserId() + "." + extension);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        user.setProfilePicture(dest.toString());
        userRepository.save(user);
    }

    public Resource getProfilePicture(String email) throws IOException {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getProfilePicture() == null) {
            throw new ResourceNotFoundException("No profile picture found");
        }
        Path filePath = Paths.get(user.getProfilePicture());

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new ResourceNotFoundException("Profile picture file not found on server");
        }

        return resource;
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
            try {
                Files.deleteIfExists(Paths.get(user.getProfilePicture()));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to delete profile picture file", e);
            }
        }
        pollingIntervalRepository.deleteByUser(user);
        settingsRepository.deleteByUser(user);
        alertRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private String imageFileExtension(String contentType) {
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed.");
        }
        int slash = contentType.indexOf('/');
        String subtype = contentType.substring(slash + 1).toLowerCase(Locale.ROOT);
        int semi = subtype.indexOf(';');
        if (semi >= 0) {
            subtype = subtype.substring(0, semi).trim();
        }
        int plus = subtype.indexOf('+');
        if (plus >= 0) {
            subtype = subtype.substring(0, plus);
        }
        return switch (subtype) {
            case "jpeg", "jpg" -> "jpeg";
            case "png", "gif", "webp" -> subtype;
            default -> throw new IllegalArgumentException("Unsupported image type.");
        };
    }
}
