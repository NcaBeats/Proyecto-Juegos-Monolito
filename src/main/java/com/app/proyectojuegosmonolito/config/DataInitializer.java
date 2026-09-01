package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.game.model.Category;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameState;
import com.app.proyectojuegosmonolito.game.service.CategoryService;
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
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GameService gameService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final WalletService walletService;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        if (gameService.count() > 0) {
            return;
        }

        var action = createCategory("Action");
        var adventure = createCategory("Adventure");
        var rpg = createCategory("RPG");
        var shooter = createCategory("Shooter");
        var platformer = createCategory("Platformer");
        var fighting = createCategory("Fighting");
        var openWorld = createCategory("Open World");
        var sports = createCategory("Sports");
        var indie = createCategory("Indie");
        var stealth = createCategory("Stealth");
        var horror = createCategory("Horror");
        var simulation = createCategory("Simulation");
        var racing = createCategory("Racing");
        var strategy = createCategory("Strategy");

        gameService.create(Game.builder()
                .name("Minecraft")
                .originalPrice(new BigDecimal("29.99"))
                .discountPercent(0)
                .description("Build and explore infinite worlds")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2011, 11, 18))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149050/sjclgg4kv5nvvooh4acf.webp")
                .categories(List.of(indie, adventure))
                .build());

        gameService.create(Game.builder()
                .name("Stardew Valley")
                .originalPrice(new BigDecimal("14.99"))
                .discountPercent(0)
                .description("Grow crops and build a life in the countryside")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 2, 26))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149138/wrpzkzlrasq3boe9x2vp.webp")
                .categories(List.of(indie, rpg))
                .build());

        gameService.create(Game.builder()
                .name("Elden Ring")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(0)
                .description("The Golden Order has been broken")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2022, 2, 25))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149161/q5zkmswvhs13xkkrsbcz.webp")
                .categories(List.of(action, rpg, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Rocket League")
                .originalPrice(new BigDecimal("0.00"))
                .discountPercent(0)
                .description("High-powered hybrid of arcade-style soccer and vehicular mayhem")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2015, 7, 7))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149186/jlvjpqerig4jsh4f7ypr.webp")
                .categories(List.of(sports))
                .build());

        gameService.create(Game.builder()
                .name("Spider-Man Remastered")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(40)
                .description("Swing through Marvel's New York as Spider-Man")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2022, 8, 12))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149206/xrjf04vgulidbc4lbyf3.avif")
                .categories(List.of(action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Grand Theft Auto V")
                .originalPrice(new BigDecimal("29.99"))
                .discountPercent(50)
                .description("Explore the sprawling world of Los Santos and Blaine County")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2013, 9, 17))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149231/pfmjtit08yk4aoikwfqv.webp")
                .categories(List.of(action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Battlefield 1")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(75)
                .description("Experience the dawn of all-out war in World War I")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 10, 21))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149249/yuuilkytoh575uzxosfd.webp")
                .categories(List.of(shooter, action))
                .build());

        gameService.create(Game.builder()
                .name("Assassin's Creed Shadows")
                .originalPrice(new BigDecimal("69.99"))
                .discountPercent(20)
                .description("Become a lethal shinobi Assassin and a powerful samurai")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2025, 3, 20))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149264/ixpm2q4qxzrddzuxbbla.webp")
                .categories(List.of(action, adventure, rpg, stealth))
                .build());

        gameService.create(Game.builder()
                .name("Cyberpunk 2077")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(60)
                .description("An open-world action-adventure RPG set in the megalopolis of Night City")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2020, 12, 10))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149286/pzyxwb5xndhfrs9quhlr.webp")
                .categories(List.of(rpg, action, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Dragon Ball FighterZ")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(70)
                .description("The ultimate anime fighting game powered by Arc System Works")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2018, 1, 26))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149302/qir5n5rdwxwanuzd4x4u.webp")
                .categories(List.of(fighting, action))
                .build());

        gameService.create(Game.builder()
                .name("Mortal Kombat 11")
                .originalPrice(new BigDecimal("49.99"))
                .discountPercent(80)
                .description("The latest installment in the legendary Mortal Kombat franchise")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2019, 4, 23))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149323/m9njptc1b0tthgombbgy.webp")
                .categories(List.of(fighting, action))
                .build());

        gameService.create(Game.builder()
                .name("Red Dead Redemption 2")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(67)
                .description("America, 1899. Arthur Morgan and the Van der Linde gang are on the run")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2018, 10, 26))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149472/y2wcocuowgind8c8jgza.avif")
                .categories(List.of(action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("Hollow Knight: Silksong")
                .originalPrice(new BigDecimal("29.99"))
                .discountPercent(0)
                .description("Forge your own path in Hollow Knight Silksong")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2025, 7, 17))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149493/kdq0puz7vosdiun1vvkt.webp")
                .categories(List.of(indie, platformer, adventure))
                .build());

        gameService.create(Game.builder()
                .name("The Witcher 3")
                .originalPrice(new BigDecimal("39.99"))
                .discountPercent(85)
                .description("As war rages across the Continent, you take on the greatest contract of your life")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2015, 5, 19))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788149509/co60ksh6jnzks1mzhkik.webp")
                .categories(List.of(rpg, action, adventure, openWorld))
                .build());

        gameService.create(Game.builder()
                .name("God of War Ragnarök")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(0)
                .description("Embark on an epic and heartfelt journey as Kratos and Atreus struggle with holding on and letting go")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2024, 9, 19))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788151900/tcixcrqzkgrrrihoiref.webp")
                .categories(List.of(action, adventure, rpg))
                .build());

        gameService.create(Game.builder()
                .name("Resident Evil 4")
                .originalPrice(new BigDecimal("39.99"))
                .discountPercent(30)
                .description("Survive a rural nightmare in this reimagining of the genre-defining masterpiece")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2023, 3, 24))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152018/kqhok1e1hqpwmtkoxpnd.webp")
                .categories(List.of(horror, action))
                .build());

        gameService.create(Game.builder()
                .name("Call of Duty: Modern Warfare III")
                .originalPrice(new BigDecimal("69.99"))
                .discountPercent(25)
                .description("The ultimate multiplayer experience with a massive arsenal of maps and modes")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2023, 11, 10))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152199/q9wkj9p7pwkp7mfl2il6.webp")
                .categories(List.of(shooter, action))
                .build());

        gameService.create(Game.builder()
                .name("Halo Infinite")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(50)
                .description("The legendary series returns with a vast open-world campaign and epic multiplayer")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2021, 12, 8))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152365/y2ha15uhuawclxfqs17g.webp")
                .categories(List.of(shooter, action))
                .build());

        gameService.create(Game.builder()
                .name("Forza Horizon 4")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(60)
                .description("Race, stunt, and explore the beautiful open world of Britain")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2018, 10, 2))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152509/slibpbfpsr8maroiffdi.webp")
                .categories(List.of(racing, sports))
                .build());

        gameService.create(Game.builder()
                .name("Civilization VI")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(75)
                .description("Build an empire to stand the test of time and lead your civilization from the Stone Age to the Information Age")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2016, 10, 21))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152670/ulge1bf2tbmcexewxnh2.avif")
                .categories(List.of(strategy))
                .build());

        gameService.create(Game.builder()
                .name("Silent Hill 2")
                .originalPrice(new BigDecimal("69.99"))
                .discountPercent(0)
                .description("A man receives a letter from his deceased wife and travels to the fog-shrouded town of Silent Hill")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2024, 10, 8))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152834/fdzr9lxwibrx1cmdwt8a.webp")
                .categories(List.of(horror, adventure))
                .build());

        gameService.create(Game.builder()
                .name("Euro Truck Simulator 2")
                .originalPrice(new BigDecimal("19.99"))
                .discountPercent(0)
                .description("Travel across a scaled-down Europe, pick up cargo and deliver it across beautiful landscapes")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2012, 10, 18))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788152907/q2jmubh8qbjb0jgsih5c.avif")
                .categories(List.of(simulation, indie))
                .build());

        gameService.create(Game.builder()
                .name("Sekiro: Shadows Die Twice")
                .originalPrice(new BigDecimal("59.99"))
                .discountPercent(50)
                .description("Carve your own clever path to vengeance in an all-new adventure from FromSoftware")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2019, 3, 22))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788153006/l2zmofnyufiwvlxerayj.webp")
                .categories(List.of(action, adventure))
                .build());

        gameService.create(Game.builder()
                .name("The Sims 4")
                .originalPrice(new BigDecimal("39.99"))
                .discountPercent(50)
                .description("Create unique Sims, build the perfect home, and explore vibrant worlds")
                .state(GameState.AVAILABLE)
                .launchDate(LocalDate.of(2014, 9, 2))
                .imageUrl("https://res.cloudinary.com/tpjbimjw/image/upload/v1788153094/rbqhx5vnpk1crm3antvw.webp")
                .categories(List.of(simulation))
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

    private Category createCategory(String name) {
        return categoryService.create(Category.builder().name(name).build());
    }
}
