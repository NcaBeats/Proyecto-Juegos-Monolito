package com.app.proyectojuegosmonolito.account.user.dto;

import com.app.proyectojuegosmonolito.account.profile.model.ValidEmailDomain;
import jakarta.validation.constraints.*;

public record UserRequestCreate(
    @NotBlank @Size(max = 25) String username,
    @NotBlank @Email @Size(max = 100) @ValidEmailDomain String email,
    @NotBlank @Size(min = 4, max = 10) String password
) {}
