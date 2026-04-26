package com.dxc.iotmonitor.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private String message;

}
