package com.app.proyectojuegosmonolito.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "profile")
public class Profile {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String avatarImage;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility;

    @Column(nullable = false)
    private Instant createdAt;

    public Profile update(String nickname, String avatarImage, String bio, Visibility visibility) {
        this.nickname = nickname;
        this.avatarImage = avatarImage;
        this.bio = bio;
        this.visibility = visibility;
        return this;
    }
}
