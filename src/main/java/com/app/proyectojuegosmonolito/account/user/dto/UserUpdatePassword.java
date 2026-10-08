package com.app.proyectojuegosmonolito.account.user.dto;

import jakarta.validation.constraints.NotBlank;
import com.app.proyectojuegosmonolito.validation.PasswordRules;
import jakarta.validation.constraints.Size;

public record UserUpdatePassword(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = PasswordRules.MIN, max = PasswordRules.MAX) String newPassword
) {
}
