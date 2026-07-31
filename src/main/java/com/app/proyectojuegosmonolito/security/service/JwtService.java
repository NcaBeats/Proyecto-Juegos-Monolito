package com.app.proyectojuegosmonolito.security.service;

import com.app.proyectojuegosmonolito.account.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration}")
    private long expiration;

    public String generateAccessToken(User user) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer("proyecto-juegos")
                .subject(user.getId().toString())
                .claim("type", "access")
                .claim("name", user.getUsername())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiresAt(now.plusMillis(expiration))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
