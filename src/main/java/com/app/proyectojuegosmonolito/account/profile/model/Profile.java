package com.app.proyectojuegosmonolito.account.profile.model;

import com.app.proyectojuegosmonolito.account.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

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

    @NotBlank
    @Size(min = 7, max = 9)
    @Column(nullable = false, unique = true, length = 9)
    private String run;

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Region region;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String comuna;

    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false)
    private Instant createdAt;

    public Profile update(String nickname, String avatarImage, String bio, Visibility visibility) {
        this.nickname = nickname;
        this.avatarImage = avatarImage;
        this.bio = bio;
        this.visibility = visibility;
        return this;
    }

    public Profile updatePersonalInfo(String firstName, String lastName, LocalDate birthDate,
                                      Region region, String comuna, String address) {
        if (firstName != null) this.firstName = firstName;
        if (lastName != null) this.lastName = lastName;
        if (birthDate != null) this.birthDate = birthDate;
        if (region != null) this.region = region;
        if (comuna != null) this.comuna = comuna;
        if (address != null) this.address = address;
        return this;
    }
}
