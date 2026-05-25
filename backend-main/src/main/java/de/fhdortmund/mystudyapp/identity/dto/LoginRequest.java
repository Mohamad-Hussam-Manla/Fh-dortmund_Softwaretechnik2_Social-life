package de.fhdortmund.mystudyapp.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    private String universityEmail;

    @NotBlank(message = "Password is required")
    private String password;
}