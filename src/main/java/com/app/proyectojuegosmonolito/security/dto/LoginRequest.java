package com.app.proyectojuegosmonolito.security.dto;

import com.app.proyectojuegosmonolito.account.profile.model.ValidEmailDomain;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email @Size(max = 100) @ValidEmailDomain String email,
        @NotBlank @Size(min = 4, max = 10) String password
) {}
