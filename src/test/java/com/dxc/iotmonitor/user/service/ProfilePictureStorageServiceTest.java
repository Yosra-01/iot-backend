package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.user.config.R2StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ProfilePictureStorageServiceTest {

    private S3Client s3Client;
    private ProfilePictureStorageService service;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        R2StorageProperties properties = new R2StorageProperties();
        properties.setAccountId("account");
        properties.setBucket("sensorix-profile-images");
        properties.setAccessKeyId("access");
        properties.setSecretAccessKey("secret");
        properties.setPublicBaseUrl("https://cdn.example.com");
        properties.setProfilePicturePrefix("profile-pictures");
        service = new ProfilePictureStorageService(s3Client, properties);
    }

    @Test
    void upload_validImage_putsObjectAndReturnsPublicUrl() throws IOException {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.jpg",
                "image/jpeg",
                "fake-image-bytes".getBytes());

        String result = service.upload(userId, file);

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        PutObjectRequest request = requestCaptor.getValue();
        assertEquals("sensorix-profile-images", request.bucket());
        assertTrue(request.key().matches("profile-pictures/" + userId + "/\\d+\\.jpeg"));
        assertEquals(file.getSize(), request.contentLength());
        assertEquals("image/jpeg", request.contentType());
        assertEquals("https://cdn.example.com/" + request.key(), result);
    }

    @Test
    void upload_nonImageContentType_rejectsFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "fake-pdf".getBytes());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.upload(UUID.randomUUID(), file));

        assertEquals("Only image files are allowed.", exception.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void upload_unsupportedImageType_rejectsFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.bmp",
                "image/bmp",
                "fake-image".getBytes());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.upload(UUID.randomUUID(), file));

        assertEquals("Unsupported image type.", exception.getMessage());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void deleteByPublicUrl_matchingCdnUrl_deletesObject() {
        service.deleteByPublicUrl("https://cdn.example.com/profile-pictures/user/pic.jpeg");

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());
        DeleteObjectRequest request = requestCaptor.getValue();
        assertEquals("sensorix-profile-images", request.bucket());
        assertEquals("profile-pictures/user/pic.jpeg", request.key());
    }

    @Test
    void deleteByPublicUrl_legacyLocalPath_isNoOp() {
        service.deleteByPublicUrl("uploads/profile-pictures/user.jpeg");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void objectKeyFromPublicUrl_blankValue_returnsEmpty() {
        assertFalse(service.objectKeyFromPublicUrl(" ").isPresent());
    }
}
