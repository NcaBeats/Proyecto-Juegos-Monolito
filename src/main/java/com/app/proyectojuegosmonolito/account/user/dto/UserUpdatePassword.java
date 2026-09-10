package com.app.proyectojuegosmonolito.account.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdatePassword(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = 4, max = 10) String newPassword
) {
}
