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
    // Possessive / negated-class forms avoid super-linear backtracking (java:S8786).
    @Pattern(regexp = "^$|[^a-z]*+[a-z][\\s\\S]*+", message = "must contain a lowercase letter")
    @Pattern(regexp = "^$|[^A-Z]*+[A-Z][\\s\\S]*+", message = "must contain an uppercase letter")
    @Pattern(regexp = "^$|\\D*+\\d[\\s\\S]*+", message = "must contain a digit")
    @Pattern(regexp = "^$|[^@$!%*?&]*+[@$!%*?&][\\s\\S]*+", message = "must contain a special character")
    @Pattern(
            regexp = "^$|^[A-Za-z\\d@$!%*?&]*+$",
            message = "only letters, digits and the special characters @$!%*?& are allowed"
    )
    private String newPassword;
}
