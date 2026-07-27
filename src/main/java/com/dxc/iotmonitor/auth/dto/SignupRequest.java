package com.dxc.iotmonitor.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "email is required")
    //@Email(message = "invalid email format")
    @Pattern(
            regexp = "^([a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,})?$",
            message = "invalid email format"
    )
    private String email;

    @NotBlank(message = "first name is required")
    @Pattern(
            regexp = "^([a-zA-Z]([a-zA-Z\\s\\-\\']*[a-zA-Z])?)?$",
            message = "invalid first name"
    )
    private String firstName;

    @NotBlank(message = "last name is required")
    @Pattern(
            regexp = "^([a-zA-Z]([a-zA-Z\\s\\-\\']*[a-zA-Z])?)?$",
            message = "invalid last name"
    )
    private String lastName;

    @NotBlank(message = "password is required")
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
    private String password;

}
