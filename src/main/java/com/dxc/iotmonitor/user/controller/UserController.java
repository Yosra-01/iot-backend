package com.dxc.iotmonitor.user.controller;

import com.dxc.iotmonitor.user.dto.ProfileResponse;
import com.dxc.iotmonitor.user.dto.UpdatePasswordRequest;
import com.dxc.iotmonitor.user.dto.UpdateProfilePictureRequest;
import com.dxc.iotmonitor.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final UserService userService;

    //Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        ProfileResponse response = userService.getProfile(email);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    //Change User Profile Picture
    @PatchMapping("/profile/picture")
    public ResponseEntity<?> updateProfilePicture(@RequestBody @Valid UpdateProfilePictureRequest request){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updateProfilePicture(email, request);
        return ResponseEntity.ok(Map.of("message", "Profile picture updated successfully."));
    }

    //Change User Password
    @PatchMapping("/profile/password")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid UpdatePasswordRequest request){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.updatePassword(email, request);
        return ResponseEntity.ok(Map.of("message","Password updated successfully."));
    }

    //Delete User By email -> for automated testing purposes
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserByEmail(@RequestParam String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully."));
    }
}
