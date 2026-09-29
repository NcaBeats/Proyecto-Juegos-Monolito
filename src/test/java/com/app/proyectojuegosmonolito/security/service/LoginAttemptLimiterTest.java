package com.app.proyectojuegosmonolito.security.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptLimiterTest {

    private static final String EMAIL = "player1@gmail.com";
    private static final String IP = "203.0.113.7";

    private LoginAttemptLimiter limiter(int maxPerEmail, int maxPerIp, long windowMillis) {
        return new LoginAttemptLimiter(maxPerEmail, maxPerIp, windowMillis);
    }

    @Test
    void retryAfter_shouldBeZero_whenNoFailures() {
        var limiter = limiter(5, 20, 900_000L);

        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isZero();
    }

    @Test
    void retryAfter_shouldStayZero_belowTheEmailLimit() {
        var limiter = limiter(5, 20, 900_000L);

        for (int i = 0; i < 4; i++) {
            limiter.onFailure(EMAIL, IP);
        }

        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isZero();
    }

    @Test
    void retryAfter_shouldBePositive_atTheEmailLimit() {
        var limiter = limiter(5, 20, 900_000L);

        for (int i = 0; i < 5; i++) {
            limiter.onFailure(EMAIL, IP);
        }

        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isBetween(1L, 900L);
    }

    @Test
    void onSuccess_shouldClearTheEmailCounter() {
        var limiter = limiter(2, 20, 900_000L);
        limiter.onFailure(EMAIL, IP);
        limiter.onFailure(EMAIL, IP);
        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isPositive();

        limiter.onSuccess(EMAIL, IP);

        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isZero();
    }

    @Test
    void shouldBlockByIp_whenManyDistinctEmailsAreTried() {
        var limiter = limiter(5, 3, 900_000L);

        // Tres correos distintos, un fallo cada uno: el limite por email no se
        // alcanza, pero el de IP si.
        limiter.onFailure("a@test.com", IP);
        limiter.onFailure("b@test.com", IP);
        limiter.onFailure("c@test.com", IP);

        assertThat(limiter.retryAfterSeconds("d@test.com", IP)).isPositive();
    }

    @Test
    void emailKey_shouldBeCaseAndWhitespaceInsensitive() {
        var limiter = limiter(2, 20, 900_000L);
        limiter.onFailure("  Player1@Gmail.com ", IP);
        limiter.onFailure("player1@gmail.com", IP);

        assertThat(limiter.retryAfterSeconds("PLAYER1@GMAIL.COM", IP)).isPositive();
    }

    @Test
    void shouldResetTheCounter_afterTheWindowExpires() {
        // Ventana de 1 ms: al expirar, el contador arranca de cero.
        var limiter = limiter(2, 20, 1L);
        limiter.onFailure(EMAIL, IP);
        limiter.onFailure(EMAIL, IP);
        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isPositive();

        sleep(5);

        assertThat(limiter.retryAfterSeconds(EMAIL, IP)).isZero();
    }

    @Test
    void shouldTrackEachEmailIndependently() {
        var limiter = limiter(2, 20, 900_000L);
        limiter.onFailure(EMAIL, IP);
        limiter.onFailure(EMAIL, IP);

        assertThat(limiter.retryAfterSeconds("otro@gmail.com", IP)).isZero();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
