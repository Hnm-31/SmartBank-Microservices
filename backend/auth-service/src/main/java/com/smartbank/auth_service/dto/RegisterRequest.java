package com.smartbank.auth_service.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$", message = "Password must contain at least one digit and one special character")
    private String password;
    
    private String role; // "USER" or "ADMIN"
}
