package com.dxc.iotmonitor.user.service;

import com.dxc.iotmonitor.exception.ResourceNotFoundException;
import com.dxc.iotmonitor.user.model.User;
import com.dxc.iotmonitor.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        if (user == null){
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        userRepository.delete(user);
    }
}
