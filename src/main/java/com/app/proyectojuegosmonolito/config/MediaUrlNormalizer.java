package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.MediaUrl;
import com.app.proyectojuegosmonolito.game.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Normaliza a URLs absolutas las filas que todavia guardan la ruta legacy del
 * trailer ({@code /uploads/games/{slug}/trailer.mp4}).
 *
 * <p>El servicio ya no emite rutas relativas, asi que en una base creada con el
 * codigo actual no hay nada que hacer y el runner solo lo reporta. Cumple dos
 * funciones: dejar datos viejos en la forma nueva, y —quizas mas importante—
 * decir en el arranque cuantas filas findo y reescribio, para que no haya que
 * adivinarlo con una consulta a mano.
 *
 * <p>Solo toca columnas de texto. No sube, borra ni renombra nada en R2: el
 * objeto ya existe bajo esa misma clave, porque lo que se guarda es el prefijo
 * {@code /uploads/games/} seguido de la clave exacta dentro del bucket.
 *
 * <p>Se activa con {@code app.media.normalize.enabled} y es idempotente: las
 * filas ya normalizadas no vuelven a matchear, asi que correrlo dos veces no
 * cambia nada.
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "app.media.normalize.enabled", havingValue = "true")
@RequiredArgsConstructor
public class MediaUrlNormalizer implements CommandLineRunner {

    private final GameService gameService;

    @Value("${app.r2.public-base-url}")
    private String publicBaseUrl;

    @Override
    @Transactional
    public void run(String... args) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            // No se intenta normalizar sin base: escribiria una URL que no resuelve.
            log.error("MediaUrlNormalizer: app.r2.public-base-url esta vacio, no se normaliza nada");
            return;
        }

        int[] rewritten = new int[4];
        List<Game> toSave = new ArrayList<>();

        for (Game game : gameService.findAll()) {
            boolean changed = rewrite(game, rewritten);
            if (changed) {
                toSave.add(game);
            }
        }

        // saveAll es el unico metodo que persiste sin validar la forma: este runner
        // existe justamente para poder escribir filas que antes incumplian la regla.
        gameService.saveAll(toSave);

        log.info(
                "MediaUrlNormalizer: found={} rewritten={} (video={} image={} banner={} gallery={})",
                toSave.size(), toSave.size(), rewritten[0], rewritten[1], rewritten[2], rewritten[3]);
    }

    /**
     * @return true si la entidad quedo modificada
     */
    private boolean rewrite(Game game, int[] counter) {
        boolean changed = false;

        if (MediaUrl.isLegacyRelative(game.getVideoUrl())) {
            game.setVideoUrl(MediaUrl.toAbsolute(game.getVideoUrl(), publicBaseUrl));
            counter[0]++;
            changed = true;
        }
        if (MediaUrl.isLegacyRelative(game.getImageUrl())) {
            game.setImageUrl(MediaUrl.toAbsolute(game.getImageUrl(), publicBaseUrl));
            counter[1]++;
            changed = true;
        }
        if (MediaUrl.isLegacyRelative(game.getBannerUrl())) {
            game.setBannerUrl(MediaUrl.toAbsolute(game.getBannerUrl(), publicBaseUrl));
            counter[2]++;
            changed = true;
        }
        if (game.getGallery() != null) {
            for (var image : game.getGallery()) {
                if (MediaUrl.isLegacyRelative(image.getUrl())) {
                    image.setUrl(MediaUrl.toAbsolute(image.getUrl(), publicBaseUrl));
                    counter[3]++;
                    changed = true;
                }
            }
        }
        return changed;
    }
}