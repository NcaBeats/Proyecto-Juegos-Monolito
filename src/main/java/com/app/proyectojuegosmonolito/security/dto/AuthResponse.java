package com.app.proyectojuegosmonolito.security.dto;

public record AuthResponse(
        String token,
        long expiresIn
) {
}
