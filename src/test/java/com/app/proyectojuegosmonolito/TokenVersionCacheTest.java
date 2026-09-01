package com.app.proyectojuegosmonolito;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TokenVersionCacheTest {

    @Test
    void get_whenAbsent_shouldReturnEmpty() {
        var cache = new TokenVersionCache(60_000);

        assertThat(cache.get(1L)).isEmpty();
    }

    @Test
    void setAndGet_shouldReturnStoredVersion() {
        var cache = new TokenVersionCache(60_000);

        cache.set(1L, 3);

        assertThat(cache.get(1L)).isEqualTo(Optional.of(3));
    }

    @Test
    void set_shouldOverwritePreviousVersion() {
        var cache = new TokenVersionCache(60_000);
        cache.set(1L, 1);

        cache.set(1L, 2);

        assertThat(cache.get(1L)).isEqualTo(Optional.of(2));
    }

    @Test
    void get_afterTtlExpiry_shouldReturnEmpty() throws InterruptedException {
        var cache = new TokenVersionCache(1);

        cache.set(1L, 3);
        Thread.sleep(5);

        assertThat(cache.get(1L)).isEmpty();
    }

    @Test
    void entries_shouldBeIsolatedByUserId() {
        var cache = new TokenVersionCache(60_000);

        cache.set(1L, 5);

        assertThat(cache.get(2L)).isEmpty();
    }
}
