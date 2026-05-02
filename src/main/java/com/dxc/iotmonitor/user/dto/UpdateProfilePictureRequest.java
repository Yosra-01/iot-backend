package com.dxc.iotmonitor.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfilePictureRequest {

    @NotBlank(message = "profile picture is required")
    private String profilePicture;
}
