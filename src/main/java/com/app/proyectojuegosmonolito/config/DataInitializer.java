package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.account.user.model.Role;
import com.app.proyectojuegosmonolito.account.user.model.User;
import com.app.proyectojuegosmonolito.account.user.service.UserService;
import com.app.proyectojuegosmonolito.account.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GameService gameService;
    private final UserService userService;
    private final WalletService walletService;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        if (gameService.count() > 0) {
            return;
        }

        gameService.create(Game.builder()
                .name("Minecraft")
                .price(new BigDecimal("29.99"))
                .description("Build and explore infinite worlds")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2011, 11, 18))
                .build());

        gameService.create(Game.builder()
                .name("Stardew Valley")
                .price(new BigDecimal("14.99"))
                .description("Grow crops and build a life in the countryside")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 2, 26))
                .build());

        gameService.create(Game.builder()
                .name("Elden Ring")
                .price(new BigDecimal("59.99"))
                .description("The Golden Order has been broken")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2022, 2, 25))
                .build());

        var player1 = userService.create(User.builder()
                .username("player1")
                .email("player1@test.com")
                .password("pass123")
                .role(Role.ADMIN)
                .build());
        walletService.updateBalance(player1.getId(), new BigDecimal("200"));

        var player2 = userService.create(User.builder()
                .username("player2")
                .email("player2@test.com")
                .password("pass123")
                .build());
        walletService.updateBalance(player2.getId(), new BigDecimal("50"));

        userService.create(User.builder()
                .username("broke_player")
                .email("broke@test.com")
                .password("pass123")
                .build());
    }
}
