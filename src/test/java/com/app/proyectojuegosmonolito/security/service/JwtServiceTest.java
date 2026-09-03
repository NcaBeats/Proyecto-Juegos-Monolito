package com.app.proyectojuegosmonolito.security.service;

import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-0123456789abcdefghijklmnopqrstuvwxyz";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final JwtService jwtService;

    JwtServiceTest() {
        var key = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
        encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(MacAlgorithm.HS256).build();
        decoder = NimbusJwtDecoder.withSecretKey(key).build();
        jwtService = new JwtService(encoder);
        setExpiration(3600000);
    }

    @Test
    void generateAccessToken_shouldIncludeVersionClaim() {
        var user = User.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .createdAt(Instant.now())
                .role(Role.CLIENTE)
                .tokenVersion(3)
                .build();

        var token = jwtService.generateAccessToken(user);
        var jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getClaimAsString("ver")).isEqualTo("3");
        assertThat(jwt.getClaimAsString("role")).isEqualTo("CLIENTE");
    }

    private void setExpiration(long millis) {
        try {
            Field field = JwtService.class.getDeclaredField("expiration");
            field.setAccessible(true);
            field.setLong(jwtService, millis);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
