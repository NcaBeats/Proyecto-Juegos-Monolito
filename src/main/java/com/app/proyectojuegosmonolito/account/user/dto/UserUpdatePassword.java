package com.app.proyectojuegosmonolito.account.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserUpdatePassword(
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {
}
