package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.exception.InvalidCredentialsException;
import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.dto.UpdateProfilePictureRequest;
import com.dxc.iotmonitor.user.mapper.UserMapper;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileResponse getProfile(String email){
        User user = userRepository.findByEmailIgnoreCase(email);
        ProfileResponse response = userMapper.toResponse(user);
        return response;
    }

    public void updateProfilePicture(String email, UpdateProfilePictureRequest request){
        User user = userRepository.findByEmailIgnoreCase(email);
        user.setProfilePicture(request.getProfilePicture());
        userRepository.save(user);
    }

    public void updatePassword(String email, UpdatePasswordRequest request){
        User user = userRepository.findByEmailIgnoreCase(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null){
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.delete(user);
    }
}
