package com.app.proyectojuegosmonolito.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private GameState state;

    @Column(nullable = false)
    private LocalDate launchDate;

    @Column(nullable = false)
    private Instant createdAt;

    public Game update(String name, BigDecimal price, String description, GameState state, LocalDate launchDate) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.state = state;
        this.launchDate = launchDate;
        return this;
    }
}
