package com.dxc.iotmonitor.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank(message = "current password is required")
    private String currentPassword;

    @NotBlank(message = "new password is required")
    @Size(min = 8, message = "password must at least 8 characters long")
    @Size(max = 64, message = "password too long. 64 characters is the maximum") //DOS prevention -> without max, a long password would take forever to be hashed
    @Pattern(
            regexp = "^((?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+)?$",
            message = "password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String newPassword;
}
