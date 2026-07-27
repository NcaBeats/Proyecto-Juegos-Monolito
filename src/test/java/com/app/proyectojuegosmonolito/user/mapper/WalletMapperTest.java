package com.app.proyectojuegosmonolito.user.mapper;

import com.app.proyectojuegosmonolito.user.dto.WalletResponse;
import com.app.proyectojuegosmonolito.user.model.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class WalletMapperTest {

    private final WalletMapper mapper = new WalletMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        var updatedAt = Instant.parse("2026-01-01T00:00:00Z");
        var wallet = Wallet.builder()
                .userId(1L).balance(new BigDecimal("99.99"))
                .updatedAt(updatedAt)
                .build();

        var result = mapper.toResponse(wallet);

        assertThat(result).isEqualTo(new WalletResponse(1L, new BigDecimal("99.99"), updatedAt));
    }
}
