package com.app.proyectojuegosmonolito.security.dto;

import com.app.proyectojuegosmonolito.validation.ValidEmailDomain;
import com.app.proyectojuegosmonolito.validation.PasswordRules;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email @Size(max = 100) @ValidEmailDomain String email,
        @NotBlank @Size(min = PasswordRules.MIN, max = PasswordRules.MAX) String password
) {}
