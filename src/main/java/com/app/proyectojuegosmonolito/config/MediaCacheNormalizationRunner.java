package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.game.storage.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Runner de normalizacion de la media ya subida (detras de
 * {@code app.r2.normalize-media=true}), idempotente y pensado para correrse una vez.
 * <p>
 * El catalogo se creo antes de que los uploads llevaran Cache-Control, asi que los
 * objetos que ya estan en R2 se sirven sin cachear y el navegador los vuelve a pedir
 * en cada visita. Este runner los normaliza sin mover bytes:
 *
 * <ul>
 *   <li><b>Trailers</b>: calcula la huella del contenido (prefijo de 16 MiB, no el
 *       archivo completo), copia el objeto a la clave versionada
 *       {@code {directorio}/trailer-{huella}.mp4}, le aplica la politica de cache,
 *       reescribe {@code videoUrl} y borra la clave anterior.</li>
 *   <li><b>Resto de la media</b>: solo aplica la politica. Las imagenes ya viven en
 *       claves con UUID, que son unicas por version, asi que {@code immutable} es
 *       seguro ahi.</li>
 * </ul>
 *
 * Idempotente: un objeto que ya tiene la politica esperada no se toca y un trailer ya
 * versionado no se vuelve a copiar. Un trailer ausente en el bucket, o cuya huella no
 * se pudo calcular, se deja como estaba: preferimos un trailer sin cachear antes que
 * perderlo. El borrado de la clave anterior se hace despues de guardar el catalogo,
 * porque hasta ahi la fila sigue apuntando al objeto viejo.
 */
@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "app.r2.normalize-media", havingValue = "true")
@RequiredArgsConstructor
public class MediaCacheNormalizationRunner implements ApplicationRunner {

    private static final String TRAILER_BASE = "trailer";
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".webm", ".mov", ".m4v");

    private final GameService gameService;
    private final R2StorageService r2StorageService;

    @Override
    public void run(ApplicationArguments args) {
        List<Game> dirtyGames = new ArrayList<>();
        List<String> supersededKeys = new ArrayList<>();
        int versioned = 0;
        int headersApplied = 0;

        for (Game game : gameService.findAllWithGallery()) {
            String previousVideoUrl = game.getVideoUrl();
            if (versionTrailer(game)) {
                dirtyGames.add(game);
                versioned++;
                var previousKey = r2StorageService.keyOf(previousVideoUrl);
                var currentKey = r2StorageService.keyOf(game.getVideoUrl());
                if (previousKey != null && !previousKey.equals(currentKey)) {
                    supersededKeys.add(previousKey);
                }
            }
            headersApplied += normalizeImageHeaders(game);
        }

        if (!dirtyGames.isEmpty()) {
            gameService.saveAll(dirtyGames);
        }

        // Solo ahora la fila apunta al objeto nuevo, asi que la clave anterior ya no
        // la referencia nadie.
        supersededKeys.forEach(key -> r2StorageService.deleteTrailer(key));

        headersApplied += normalizeOrphanHeaders();

        log.info("Media cache normalization finished: trailersVersioned={}, headersApplied={}, " +
                        "supersededTrailersDeleted={}, gamesRewritten={}",
                versioned, headersApplied, supersededKeys.size(), dirtyGames.size());
        log.warn("Normalization reported {} changes. Verify with SQL plus a HEAD request, then remove " +
                "APP_R2_NORMALIZE_MEDIA from Render: this runner executes on every boot while the flag is set.",
                versioned + headersApplied);
    }

    /**
     * Pasa el trailer del juego a su clave versionada y apunta la entidad al objeto
     * nuevo.
     *
     * @return {@code true} si la fila quedo dirty y hay que guardarla.
     */
    private boolean versionTrailer(Game game) {
        var sourceKey = r2StorageService.keyOf(game.getVideoUrl());
        if (sourceKey == null || !isVideo(sourceKey)) {
            return false;
        }
        if (isFingerprinted(sourceKey)) {
            // Ya versionado por un arranque anterior: solo falta el header.
            if (r2StorageService.applyCacheControl(sourceKey)) {
                log.info("Applied cache policy to already versioned trailer {}", sourceKey);
            }
            return false;
        }
        var fingerprint = r2StorageService.fingerprintOf(sourceKey);
        if (fingerprint == null) {
            log.warn("Could not fingerprint trailer {} of game {}: missing or unreadable. " +
                    "Leaving its URL untouched.", sourceKey, game.getName());
            return false;
        }
        var destinationKey = versionedKey(sourceKey, fingerprint);
        if (destinationKey == null || destinationKey.equals(sourceKey)) {
            return false;
        }
        if (!r2StorageService.copyObject(sourceKey, destinationKey)) {
            log.warn("Could not copy trailer {} to {}; keeping the old URL", sourceKey, destinationKey);
            return false;
        }
        // La copia no hereda la politica porque el origen no tenia ninguna, y
        // applyCacheControl la lee del HEAD del destino para no perder el contentType.
        r2StorageService.applyCacheControl(destinationKey);
        game.setVideoUrl(r2StorageService.publicPath(destinationKey));
        log.info("Versioned trailer {} -> {} ({})", sourceKey, destinationKey, game.getName());
        return true;
    }

    /**
     * Aplica la politica a las imagenes del juego que todavia no la tienen, sin
     * cambiar ninguna URL: la clave con UUID ya es unica por version, que es la
     * precondicion que hace aceptable declarar immutable.
     */
    private int normalizeImageHeaders(Game game) {
        int applied = 0;
        var urls = new ArrayList<String>();
        urls.add(game.getImageUrl());
        urls.add(game.getBannerUrl());
        game.getGallery().forEach(image -> urls.add(image.getUrl()));
        for (String url : urls) {
            var key = r2StorageService.keyOf(url);
            if (key != null && !isVideo(key) && r2StorageService.applyCacheControl(key)) {
                applied++;
            }
        }
        return applied;
    }

    /**
     * Barra el bucket para cubrir lo que el catalogo no referencia (imagenes de un
     * juego borrado que quedo a medio borrar, portadas de blog reescritas, etc.).
     */
    private int normalizeOrphanHeaders() {
        int applied = 0;
        for (String key : r2StorageService.listKeys()) {
            if (!isVideo(key) && r2StorageService.applyCacheControl(key)) {
                applied++;
            }
        }
        return applied;
    }

    /**
     * Inserta la huella antes de la extension, conservando el directorio original.
     * Devuelve {@code null} si el nombre no tiene la forma {@code trailer.{ext}}, para
     * que un archivo con nombre inesperado no acabe con doble extension.
     */
    private String versionedKey(String key, String fingerprint) {
        int slash = key.lastIndexOf('/');
        String directory = slash < 0 ? "" : key.substring(0, slash + 1);
        String filename = slash < 0 ? key : key.substring(slash + 1);
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return null;
        }
        String base = filename.substring(0, dot);
        String extension = filename.substring(dot);
        if (!TRAILER_BASE.equals(base)) {
            return null;
        }
        return directory + TRAILER_BASE + "-" + fingerprint + extension;
    }

    private boolean isFingerprinted(String key) {
        int slash = key.lastIndexOf('/');
        String filename = (slash < 0 ? key : key.substring(slash + 1)).toLowerCase(Locale.ROOT);
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        String base = filename.substring(0, dot);
        int dash = base.lastIndexOf('-');
        if (!base.startsWith(TRAILER_BASE + "-") || dash != TRAILER_BASE.length()) {
            return false;
        }
        String fingerprint = base.substring(dash + 1);
        return fingerprint.length() == 8 && fingerprint.chars().allMatch(c -> Character.digit(c, 16) >= 0);
    }

    private boolean isVideo(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        return VIDEO_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }
}
