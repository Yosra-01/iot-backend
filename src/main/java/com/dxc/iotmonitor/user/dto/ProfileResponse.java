package com.dxc.iotmonitor.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePicture;

}
