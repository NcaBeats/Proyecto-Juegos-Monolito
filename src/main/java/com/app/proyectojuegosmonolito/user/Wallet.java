package com.app.proyectojuegosmonolito.user;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private Instant updatedAt;

    public Wallet update(BigDecimal newBalance) {
        this.balance = newBalance;
        this.updatedAt = Instant.now();
        return this;
    }
}
