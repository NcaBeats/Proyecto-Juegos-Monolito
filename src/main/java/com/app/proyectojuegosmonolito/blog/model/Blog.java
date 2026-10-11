package com.app.proyectojuegosmonolito.blog.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "blog")
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private BlogCategory category;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Blog update(String title, String excerpt, String content, String coverImage, BlogCategory category) {
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.coverImage = coverImage;
        this.category = category;
        return this;
    }
}
