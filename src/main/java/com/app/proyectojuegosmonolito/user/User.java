package com.app.proyectojuegosmonolito.user;

import com.app.proyectojuegosmonolito.library.Library;
import com.app.proyectojuegosmonolito.purchase.Purchase;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 25, unique = true)
    private String username;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wallet wallet;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Purchase> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Library> libraries = new ArrayList<>();

    public User update(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        return this;
    }
}
