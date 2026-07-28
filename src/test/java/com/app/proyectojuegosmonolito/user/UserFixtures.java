package com.app.proyectojuegosmonolito.user;

import com.app.proyectojuegosmonolito.user.model.Profile;
import com.app.proyectojuegosmonolito.user.model.User;
import com.app.proyectojuegosmonolito.user.model.Visibility;
import com.app.proyectojuegosmonolito.user.model.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public class UserFixtures {

    public static User user() {
        return User.builder()
                .username("user")
                .email("user@test.com")
                .password("pass123")
                .createdAt(Instant.now())
                .build();
    }

    public static User user(Long id) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .email("user" + id + "@test.com")
                .password("pass123")
                .createdAt(Instant.now())
                .build();
    }

    public static User user(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("pass123")
                .createdAt(Instant.now())
                .build();
    }

    public static User user(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("pass123")
                .createdAt(Instant.now())
                .build();
    }

    public static Profile profile(User user) {
        return Profile.builder()
                .userId(user.getId())
                .user(user)
                .nickname(user.getUsername())
                .visibility(Visibility.PUBLIC)
                .createdAt(Instant.now())
                .build();
    }

    public static Profile profile(User user, String nickname, Visibility visibility) {
        return Profile.builder()
                .userId(user.getId())
                .user(user)
                .nickname(nickname)
                .visibility(visibility)
                .createdAt(Instant.now())
                .build();
    }

    public static Wallet wallet(User user, BigDecimal balance) {
        return Wallet.builder()
                .userId(user.getId())
                .user(user)
                .balance(balance)
                .updatedAt(Instant.now())
                .build();
    }
}
