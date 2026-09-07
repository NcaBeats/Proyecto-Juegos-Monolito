package com.app.proyectojuegosmonolito.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "game_image")
public class GameImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
