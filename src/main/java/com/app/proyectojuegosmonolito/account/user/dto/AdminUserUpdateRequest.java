package com.app.proyectojuegosmonolito.account.user.dto;

import com.app.proyectojuegosmonolito.account.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @NotBlank @Email @Size(max = 100) String email,
        Role role,
        @Size(min = 4, max = 10) String password
) {}
