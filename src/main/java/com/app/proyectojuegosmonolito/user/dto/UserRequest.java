package com.app.proyectojuegosmonolito.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Size(max = 25) String username,
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank @Size(max = 255) String password
) {}
