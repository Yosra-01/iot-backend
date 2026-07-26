package com.dxc.iotmonitor.auth.service;

import com.dxc.iotmonitor.auth.dto.AuthResponse;
import com.dxc.iotmonitor.auth.dto.LoginRequest;
import com.dxc.iotmonitor.auth.dto.SignupRequest;
import com.dxc.iotmonitor.auth.mapper.AuthMapper;
import com.dxc.iotmonitor.exception.DuplicateEmailException;
import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.polling.PollingIntervalRepository;
import com.dxc.iotmonitor.polling.PollingIntervalService;
import com.dxc.iotmonitor.security.JwtUtil;
import com.dxc.iotmonitor.security.TokenBlacklistService;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PollingIntervalRepository pollingIntervalRepository;

    @Mock
    private PollingIntervalService pollingIntervalService;

    @InjectMocks
    private AuthService authService;

    // ================================================================
    // createUser tests
    // ================================================================

    @Test
    void createUser_Success() {
        // Arrange
        SignupRequest request = new SignupRequest(
                "john.doe@example.com", "John", "Doe", "SecurePass1!"
        );

        User mappedUser = new User();
        User savedUser = new User();
        savedUser.setEmail("john.doe@example.com");
        AuthResponse expectedResponse = new AuthResponse();
        expectedResponse.setProfilePicture(null);

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(authMapper.toEntity(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(mappedUser)).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser.getEmail())).thenReturn("mocked-jwt-token");
        when(authMapper.toResponse(savedUser)).thenReturn(expectedResponse);

        // Act
        AuthResponse result = authService.createUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("mocked-jwt-token", result.getToken());
        assertEquals("User registered successfully.", result.getMessage());
        assertNull(result.getProfilePicture());
        verify(userRepository, times(1)).save(mappedUser);
    }

    @Test
    void createUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        // Arrange
        SignupRequest request = new SignupRequest(
                "john.doe@example.com", "John", "Doe", "SecurePass1!"
        );

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        // Act & Assert
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> authService.createUser(request)
        );

        assertEquals("Email already exists.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ================================================================
    // login tests
    // ================================================================

    @Test
    void login_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("john.doe@example.com", "securePassword123");

        User existingUser = new User();
        existingUser.setEmail("john.doe@example.com");
        existingUser.setPassword("hashedPassword");
        existingUser.setProfilePicture("https://cdn.example.com/profile-pictures/user/pic.jpeg");

        AuthResponse expectedResponse = new AuthResponse();
        expectedResponse.setProfilePicture(existingUser.getProfilePicture());

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(request.getPassword(), existingUser.getPassword())).thenReturn(true);
        when(pollingIntervalRepository.findByUser(existingUser)).thenReturn(Optional.empty());
        when(jwtUtil.generateToken(existingUser.getEmail())).thenReturn("mocked-jwt-token");
        when(authMapper.toResponse(existingUser)).thenReturn(expectedResponse);

        // Act
        AuthResponse result = authService.login(request);

        // Assert
        assertNotNull(result);
        assertEquals("mocked-jwt-token", result.getToken());
        assertEquals("Login successful.", result.getMessage());
        assertEquals(existingUser.getProfilePicture(), result.getProfilePicture());
        verify(pollingIntervalService).createDefault(existingUser);
    }

    @Test
    void login_UserNotFound_ThrowsInvalidCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("unknown@example.com", "securePassword123");

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.empty()); // user not found

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any()); // password check should never happen
    }

    @Test
    void login_WrongPassword_ThrowsInvalidCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("john.doe@example.com", "wrongPassword");

        User existingUser = new User();
        existingUser.setEmail("john.doe@example.com");
        existingUser.setPassword("hashedPassword");

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(request.getPassword(), existingUser.getPassword())).thenReturn(false); // wrong password

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(jwtUtil, never()).generateToken(any()); // token should never be generated
    }
}
