package com.dxc.iotmonitor.user.controller;

import com.dxc.iotmonitor.config.RateLimitService;
import com.dxc.iotmonitor.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("user@example.com", null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfilePicture_returnsRedirectToCdnUrl() throws Exception {
        URI imageUri = URI.create("https://cdn.example.com/profile-pictures/user/pic.jpeg");
        when(rateLimitService.tryConsumeProfile("user@example.com")).thenReturn(true);
        when(userService.getProfilePictureUri("user@example.com")).thenReturn(imageUri);

        ResponseEntity<Void> response = userController.getProfilePicture();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(imageUri, response.getHeaders().getLocation());
    }
}
