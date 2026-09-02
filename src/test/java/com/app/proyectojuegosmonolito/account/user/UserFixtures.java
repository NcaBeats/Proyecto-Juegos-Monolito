package com.app.proyectojuegosmonolito.account.user;

import com.app.proyectojuegosmonolito.account.profile.model.Profile;
import com.app.proyectojuegosmonolito.account.profile.model.Region;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.profile.model.Visibility;
import com.app.proyectojuegosmonolito.account.wallet.model.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class UserFixtures {

    private static int runCounter = 10000000;

    public static User user() {
        return User.builder()
                .username("user")
                .email("user@test.com")
                .password("pass123")
                .role(Role.CLIENTE)
                .createdAt(Instant.now())
                .build();
    }

    public static User user(Long id) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .email("user" + id + "@test.com")
                .password("pass123")
                .role(Role.CLIENTE)
                .createdAt(Instant.now())
                .build();
    }

    public static User user(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password("pass123")
                .role(Role.CLIENTE)
                .createdAt(Instant.now())
                .build();
    }

    public static User user(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("pass123")
                .role(Role.CLIENTE)
                .createdAt(Instant.now())
                .build();
    }

    public static Profile profile(User user) {
        return Profile.builder()
                .userId(user.getId())
                .user(user)
                .nickname(user.getUsername())
                .run(generateUniqueRun())
                .firstName("Test")
                .lastName("User")
                .region(Region.METROPOLITANA_DE_SANTIAGO)
                .comuna("Santiago")
                .address("Calle Test 123")
                .visibility(Visibility.PUBLIC)
                .createdAt(Instant.now())
                .build();
    }

    public static Profile profile(User user, String nickname, Visibility visibility) {
        return Profile.builder()
                .userId(user.getId())
                .user(user)
                .nickname(nickname)
                .run(generateUniqueRun())
                .firstName("Test")
                .lastName("User")
                .region(Region.METROPOLITANA_DE_SANTIAGO)
                .comuna("Santiago")
                .address("Calle Test 123")
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

    private static String generateUniqueRun() {
        return String.valueOf(runCounter++);
    }
}
