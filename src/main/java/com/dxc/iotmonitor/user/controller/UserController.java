package com.dxc.iotmonitor.user.controller;

import com.dxc.iotmonitor.config.RateLimitService;
import com.dxc.iotmonitor.exception.TooManyRequestsException;
import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private static final String MESSAGE_KEY = "message";

    private final UserService userService;
    private final RateLimitService rateLimitService;

    //Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() throws TooManyRequestsException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!rateLimitService.tryConsumeProfile(email)) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }
        ProfileResponse response = userService.getProfile(email);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    //Change User Profile Picture
    @PatchMapping("/profile/picture")
    public ResponseEntity<Map<String, String>> updateProfilePicture(@RequestParam("file") MultipartFile file)
            throws TooManyRequestsException, IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!rateLimitService.tryConsumeProfile(email)) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }

        userService.updateProfilePicture(email, file);
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Profile picture updated successfully."));
    }

    // Get User Profile Picture
    @GetMapping("/profile/picture")
    public ResponseEntity<Resource> getProfilePicture() throws TooManyRequestsException, IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!rateLimitService.tryConsumeProfile(email)) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }

        Resource resource = userService.getProfilePicture(email);
        Path filePath = Paths.get(resource.getURI());
        String contentType = Files.probeContentType(filePath);

        MediaType mediaType = contentType != null
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    //Change User Password
    @PatchMapping("/profile/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestBody @Valid UpdatePasswordRequest request)
            throws TooManyRequestsException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!rateLimitService.tryConsumeProfile(email)) {
            throw new TooManyRequestsException("Too many requests. Please try again later.");
        }

        userService.updatePassword(email, request);
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, "Password updated successfully."));
    }

    //Delete User By email -> for automated testing purposes
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUserByEmail(@RequestParam String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok(Map.of(MESSAGE_KEY, "User deleted successfully."));
    }
}
