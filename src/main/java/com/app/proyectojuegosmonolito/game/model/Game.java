package com.app.proyectojuegosmonolito.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "original_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private GameState state;

    @Column(nullable = false)
    private LocalDate launchDate;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "minimum_specs", columnDefinition = "TEXT")
    private String minimumSpecs;

    @Column(name = "recommended_specs", columnDefinition = "TEXT")
    private String recommendedSpecs;

    @Column(nullable = false)
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "game_category",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private List<Category> categories = new ArrayList<>();

    @Transient
    public BigDecimal getPrice() {
        if (originalPrice == null) {
            return null;
        }
        if (discountPercent == null || discountPercent == 0) {
            return originalPrice;
        }
        var multiplier = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(discountPercent)
                        .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP));
        return originalPrice.multiply(multiplier).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Game update(String name, BigDecimal originalPrice, Integer discountPercent, String description, GameState state, LocalDate launchDate, List<Category> categories, String imageUrl, String bannerUrl, String minimumSpecs, String recommendedSpecs) {
        this.name = name;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.description = description;
        this.state = state;
        this.launchDate = launchDate;
        this.categories = categories;
        this.imageUrl = imageUrl;
        this.bannerUrl = bannerUrl;
        this.minimumSpecs = minimumSpecs;
        this.recommendedSpecs = recommendedSpecs;
        return this;
    }
}
