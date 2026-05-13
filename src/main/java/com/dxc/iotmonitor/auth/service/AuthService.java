package com.dxc.iotmonitor.auth.service;

import com.dxc.iotmonitor.auth.dto.LoginRequest;
import com.dxc.iotmonitor.auth.dto.SignupRequest;
import com.dxc.iotmonitor.auth.dto.AuthResponse;
import com.dxc.iotmonitor.auth.mapper.AuthMapper;
import com.dxc.iotmonitor.exception.DuplicateEmailException;
import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.security.JwtUtil;
import com.dxc.iotmonitor.security.TokenBlacklistService;
import com.dxc.iotmonitor.security.UserDetailsImpl;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService implements UserDetailsService{

    private final AuthMapper authMapper;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository
                .findByEmailIgnoreCase(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public AuthResponse createUser(SignupRequest request) {

        if(userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists.");
        }

        User user = authMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        AuthResponse response = authMapper.toResponse(savedUser);
        response.setToken(token);
        response.setMessage("User registered successfully.");
        return response;
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository
                .findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        AuthResponse response = authMapper.toResponse(user);
        response.setToken(token);
        response.setMessage("Login successful.");

        return response;
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token);
    }

}
