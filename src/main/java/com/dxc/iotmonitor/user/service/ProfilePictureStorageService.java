package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.user.config.R2StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfilePictureStorageService {

    private final S3Client s3Client;
    private final R2StorageProperties properties;

    public String upload(UUID userId, MultipartFile file) throws IOException {
        String extension = imageFileExtension(file.getContentType());
        String contentType = normalizedContentType(file.getContentType());
        String key = objectKey(userId, extension);
        byte[] bytes = file.getBytes();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentLength((long) bytes.length)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(bytes));
        } catch (SdkException e) {
            throw new IllegalStateException("Failed to upload profile picture to R2", e);
        }

        return publicUrl(key);
    }

    public void deleteByPublicUrl(String publicUrl) {
        objectKeyFromPublicUrl(publicUrl).ifPresent(key -> {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build();

            try {
                s3Client.deleteObject(request);
            } catch (SdkException e) {
                throw new IllegalStateException("Failed to delete profile picture from R2", e);
            }
        });
    }

    Optional<String> objectKeyFromPublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return Optional.empty();
        }

        String base = stripTrailingSlash(properties.getPublicBaseUrl()) + "/";
        String value = publicUrl.trim();
        if (!value.startsWith(base)) {
            return Optional.empty();
        }

        String key = value.substring(base.length());
        return key.isBlank() ? Optional.empty() : Optional.of(key);
    }

    private String objectKey(UUID userId, String extension) {
        return "%s/%s/%d.%s".formatted(
                normalizedPrefix(),
                userId,
                Instant.now().toEpochMilli(),
                extension);
    }

    private String publicUrl(String key) {
        return stripTrailingSlash(properties.getPublicBaseUrl()) + "/" + key;
    }

    private String normalizedPrefix() {
        String prefix = properties.getProfilePicturePrefix();
        if (prefix == null || prefix.isBlank()) {
            return "profile-pictures";
        }
        return stripSlashes(prefix.trim());
    }

    private String normalizedContentType(String contentType) {
        return contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
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

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    private String stripSlashes(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == '/') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(start, end);
    }
}
