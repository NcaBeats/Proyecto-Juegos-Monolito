package com.app.proyectojuegosmonolito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenVersionCache {

    private record Entry(int version, long expiresAt) {
    }

    private final Map<Long, Entry> entries = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public TokenVersionCache(@Value("${app.jwt.token-version-cache-ttl:60000}") long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    public Optional<Integer> get(Long userId) {
        var entry = entries.get(userId);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt() < System.currentTimeMillis()) {
            entries.remove(userId, entry);
            return Optional.empty();
        }
        return Optional.of(entry.version());
    }

    public void set(Long userId, int version) {
        entries.put(userId, new Entry(version, System.currentTimeMillis() + ttlMillis));
    }
}
