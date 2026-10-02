package com.app.proyectojuegosmonolito.library.controller;

import com.app.proyectojuegosmonolito.TestcontainersConfiguration;
import com.app.proyectojuegosmonolito.account.user.repository.UserRepository;
import com.app.proyectojuegosmonolito.game.repository.GameRepository;
import com.app.proyectojuegosmonolito.library.model.Library;
import com.app.proyectojuegosmonolito.library.repository.LibraryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.app.proyectojuegosmonolito.account.user.UserFixtures.user;
import static com.app.proyectojuegosmonolito.game.GameFixtures.game;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contraparte para el modulo library del guard de open-in-view.
 *
 * <p>Lo que fija es una invariante de lectura, no una excepcion que hoy exista:
 * {@code Library.user} y {@code Library.game} son {@code FetchType.LAZY}, y
 * {@code LibraryService.findByUserId} no abre transaccion, asi que {@code LibraryMapper} corre en
 * el controlador y ya sin sesion de JPA. Eso es seguro unicamente porque el mapper pide
 * <em>solo identificadores</em> ({@code getUser().getId()}, {@code getGame().getId()}): el proxy
 * perezoso de un {@code ManyToOne} responde el getter del id desde la FK que ya trae cargada y no
 * inicializa. Por ahi {@code LibraryRepository} no lleva {@code @EntityGraph} y aun asi es
 * correcto, y anadir un JOIN a user y game seria traer columnas de mas sin necesidad.
 *
 * <p>Por eso el valor real del test es de regresion hacia adelante: si alguien cambia el mapper
 * para leer un campo de verdad (por ejemplo {@code getUser().getEmail()}, como si hace
 * {@code PurchaseMapper} y por ahi si necesita {@code @EntityGraph}), aca revienta con
 * {@code LazyInitializationException}. Sin sesion abierta, que es justo lo que el
 * {@code @Transactional} de los tests de controller oculta.
 *
 * <p>No lleva {@code @Transactional} a proposito, igual que
 * {@code game.controller.OpenInViewDisabledIntegrationTest}. Ademas persiste de verdad contra el
 * contenedor, asi que cada test usa nombres y correos unicos y limpia en {@code @AfterEach}.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
class LibraryOpenInViewDisabledIntegrationTest {

    @Value("${spring.jpa.open-in-view}")
    private boolean openInView;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private LibraryRepository libraryRepository;

    private String probeEmail;
    private String probeGameName;
    private Long probeUserId;
    private Long probeGameId;

    @BeforeEach
    void seedOwnedGame() {
        var suffix = UUID.randomUUID().toString().substring(0, 8);
        probeEmail = "probe" + suffix + "@test.com";
        probeGameName = "probe-game-" + suffix;

        var user = userRepository.save(user(probeEmail));
        probeUserId = user.getId();

        var game = gameRepository.save(game(probeGameName, BigDecimal.TEN));
        probeGameId = game.getId();

        libraryRepository.saveAndFlush(
                Library.builder()
                        .user(user)
                        .game(game)
                        .acquiredAt(Instant.now())
                        .build());
    }

    @AfterEach
    void cleanUp() {
        libraryRepository.deleteByUser_Id(probeUserId);
        gameRepository.deleteById(probeGameId);
        userRepository.deleteById(probeUserId);
    }

    @Test
    void openInViewIsDisabled() {
        assertThat(openInView)
                .as("spring.jpa.open-in-view debe quedar bajo spring:, no bajo app: (ver application.yaml)")
                .isFalse();
    }

    @Test
    void getMyLibraryResolvesAssociationsWithoutOpenSession() throws Exception {
        var token = jwt().jwt(b -> b.subject(probeUserId.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));

        mockMvc.perform(get("/api/v1/library").with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(probeUserId))
                .andExpect(jsonPath("$.content[0].gameId").value(probeGameId));
    }
}