package com.app.proyectojuegosmonolito.config;

import com.app.proyectojuegosmonolito.blog.model.Blog;
import com.app.proyectojuegosmonolito.blog.service.BlogService;
import com.app.proyectojuegosmonolito.game.model.Game;
import com.app.proyectojuegosmonolito.game.model.GameImage;
import com.app.proyectojuegosmonolito.game.service.GameService;
import com.app.proyectojuegosmonolito.game.storage.R2StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Runner temporal (detras de {@code app.migrate.images=true}) que deja el catalogo
 * cuadrado con Cloudflare R2 de forma idempotente:
 * <ul>
 *   <li><b>Imagenes</b>: copia byte a byte a R2 las que el seed aun referencia desde
 *       Cloudinary ({@code imageUrl}/{@code bannerUrl}/galeria/blog cover) y, a
 *       diferencia de la version inicial, reescribe el campo en la entidad con la
 *       URL publica de R2.</li>
 *   <li><b>Trailers</b>: unifica el key bajo el slug canonical del juego. Los
 *       trailers de la primer version del seed viven en slugs abreviados
 *       ({@code gta-v}, {@code ac-shadows}...) mientras las imagenes usan el slug
 *       completo; este runner copia server-side el objeto a
 *       {@code {slug}/trailer.mp4} y actualiza {@code videoUrl}. Los trailers que
 *       ya traen huella en la clave ({@code {slug}/trailer-a1b2c3d4.mp4}) se
 *       dejan intactos: su version ya viaja en el nombre.</li>
 * </ul>
 * Regla de mapeo de imagenes (misma que usa el seed):
 * {@code imageUrl -> {slug}/card/{archivo}}, {@code bannerUrl -> {slug}/banner/{archivo}},
 * {@code galeria -> {slug}/gallery/{archivo}}, {@code blog cover -> blogs/{slug}/cover.{ext}}.
 * Idempotente: salta lo que ya esta en R2 y lo que ya usa el slug canonical.
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "app.migrate.images", havingValue = "true")
@RequiredArgsConstructor
public class ImageMigrationRunner implements ApplicationRunner {

    /** Trailer con huella de contenido en la clave, p.ej. {@code slug/trailer-a1b2c3d4.mp4}. */
    private static final Pattern FINGERPRINTED_TRAILER = Pattern.compile("/trailer-[0-9a-f]{8}\\.mp4$");

    /**
     * Los trailers con huella en la clave ya son canonicos: la version viaja en el
     * nombre y cada objeto es unico. Aplanarlos a {slug}/trailer.mp4 seria
     * retroceder, y ademas dejaria un header immutable sobre una clave que la
     * siguiente subida sobrescribe, que es exactamente el caches stale que la
     * huella evita.
     */
    private final GameService gameService;
    private final BlogService blogService;
    private final R2StorageService r2StorageService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public void run(ApplicationArguments args) {
        int uploaded = 0;
        int alreadyPresent = 0;
        int failed = 0;
        int videoCopied = 0;
        int videoUrlUpdated = 0;
        List<Game> dirtyGames = new ArrayList<>();

        for (Game game : gameService.findAllWithGallery()) {
            String slug = slugify(game.getName());
            boolean dirty = false;

            MediaResult card = migrate(game.getImageUrl(), slug + "/card/" + lastSegment(game.getImageUrl()));
            uploaded += card.uploaded() ? 1 : 0;
            alreadyPresent += card.alreadyPresent() ? 1 : 0;
            failed += card.failed() ? 1 : 0;
            if (card.available()) {
                String url = r2StorageService.publicUrl(card.targetKey());
                if (!url.equals(game.getImageUrl())) {
                    game.setImageUrl(url);
                    dirty = true;
                }
            }

            MediaResult banner = migrate(game.getBannerUrl(), slug + "/banner/" + lastSegment(game.getBannerUrl()));
            uploaded += banner.uploaded() ? 1 : 0;
            alreadyPresent += banner.alreadyPresent() ? 1 : 0;
            failed += banner.failed() ? 1 : 0;
            if (banner.available()) {
                String url = r2StorageService.publicUrl(banner.targetKey());
                if (!url.equals(game.getBannerUrl())) {
                    game.setBannerUrl(url);
                    dirty = true;
                }
            }

            for (GameImage img : game.getGallery()) {
                MediaResult result = migrate(img.getUrl(), slug + "/gallery/" + lastSegment(img.getUrl()));
                if (result.uploaded()) uploaded++;
                if (result.alreadyPresent()) alreadyPresent++;
                if (result.failed()) failed++;
                if (result.available()) {
                    String url = r2StorageService.publicUrl(result.targetKey());
                    if (!url.equals(img.getUrl())) {
                        img.setUrl(url);
                        dirty = true;
                    }
                }
            }

            MigrationOutcome outcome = consolidateVideo(game, slug);
            videoCopied += outcome.copied() ? 1 : 0;
            videoUrlUpdated += outcome.urlUpdated() ? 1 : 0;
            dirty |= outcome.urlUpdated();

            if (dirty) {
                dirtyGames.add(game);
            }
        }

        if (!dirtyGames.isEmpty()) {
            gameService.saveAll(dirtyGames);
        }

        List<Blog> dirtyBlogs = new ArrayList<>();
        for (Blog blog : blogService.findAll(Pageable.unpaged()).getContent()) {
            String targetKey = "blogs/" + slugify(blog.getTitle()) + "/cover" + extOf(blog.getCoverImage());
            MediaResult result = migrate(blog.getCoverImage(), targetKey);
            if (result.uploaded()) uploaded++;
            if (result.alreadyPresent()) alreadyPresent++;
            if (result.failed()) failed++;
            if (result.available()) {
                String url = r2StorageService.publicUrl(targetKey);
                if (!url.equals(blog.getCoverImage())) {
                    blog.setCoverImage(url);
                    dirtyBlogs.add(blog);
                }
            }
        }
        if (!dirtyBlogs.isEmpty()) {
            dirtyBlogs.forEach(blogService::create);
        }

        log.info("Image migration (Cloudinary -> R2) finished: uploaded={}, alreadyPresent={}, failed={}, " +
                        "videosCopied={}, videoUrlsUpdated={}, gamesRewritten={}, blogsRewritten={}",
                uploaded, alreadyPresent, failed, videoCopied, videoUrlUpdated, dirtyGames.size(), dirtyBlogs.size());
        if (failed > 0) {
            log.warn("{} images could not be migrated (gone from Cloudinary, or mangled URLs in the seed).",
                    failed);
        }
    }

    private MigrationOutcome consolidateVideo(Game game, String slug) {
        String videoUrl = game.getVideoUrl();
        if (videoUrl == null || videoUrl.isBlank()) {
            return MigrationOutcome.none();
        }
        String sourceKey = keyOf(videoUrl);
        if (sourceKey == null || FINGERPRINTED_TRAILER.matcher(sourceKey).find()) {
            return MigrationOutcome.none();
        }
        String destKey = slug + "/trailer.mp4";
        if (sourceKey.equals(destKey)) {
            return MigrationOutcome.none();
        }

        boolean copied = false;
        if (r2StorageService.imageExists(destKey)) {
            log.info("Trailer already at canonical key {}, updating videoUrl", destKey);
        } else if (r2StorageService.imageExists(sourceKey)) {
            copied = r2StorageService.copyObject(sourceKey, destKey);
            if (!copied) {
                log.warn("Trailer copy failed for {} (source {} -> dest {}); keeping old URL", game.getName(), sourceKey, destKey);
                return MigrationOutcome.none();
            }
        } else {
            log.warn("Cannot consolidate trailer for {}: source {} does not exist and {} is missing",
                    game.getName(), sourceKey, destKey);
            return MigrationOutcome.none();
        }

        String canonicalUrl = r2StorageService.publicUrl(destKey);
        if (!canonicalUrl.equals(game.getVideoUrl())) {
            game.setVideoUrl(canonicalUrl);
        }
        return MigrationOutcome.of(copied, true);
    }

    /**
     * Deriva la key R2 a partir de la URL persistida. Acepta tanto la URL absoluta
     * {@code https://...r2.dev/{key}} como la relativa {@code /uploads/games/{key}}.
     */
    private String keyOf(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String u = url.trim();
        if (u.contains("res.cloudinary.com")) {
            return null;
        }
        if (u.startsWith("/uploads/games/")) {
            return u.substring("/uploads/games/".length());
        }
        int scheme = u.indexOf("://");
        if (scheme >= 0) {
            String rest = u.substring(scheme + 3);
            int slash = rest.indexOf('/');
            return slash < 0 ? null : rest.substring(slash + 1);
        }
        return u.contains("/") ? u : null;
    }

    private MediaResult migrate(String url, String targetKey) {
        if (url == null || url.isBlank() || !url.contains("res.cloudinary.com")) {
            return MediaResult.ofSkipped(targetKey);
        }
        try {
            if (r2StorageService.imageExists(targetKey)) {
                log.info("Already present in R2, skipping: {}", targetKey);
                return MediaResult.ofAlreadyPresent(targetKey);
            }
        } catch (RuntimeException e) {
            log.warn("Existence check failed for {} ({}): {}; will attempt upload anyway", targetKey, url, e.getMessage());
        }
        try {
            HttpResponse<byte[]> response = httpClient.send(
                    HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                log.warn("Source unreachable (status {}) for {} -> {}", response.statusCode(), url, targetKey);
                return MediaResult.ofFailed(targetKey);
            }
            r2StorageService.storeImageBytes(targetKey, response.body(), contentTypeOf(url));
            log.info("Migrated {} -> {} ({} bytes)", url, targetKey, response.body().length);
            return MediaResult.ofUploaded(targetKey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted migrating {} for {}", url, targetKey, e);
            return MediaResult.ofFailed(targetKey);
        } catch (Exception e) {
            log.warn("Could not migrate {} -> {}: {}", url, targetKey, e.getMessage());
            return MediaResult.ofFailed(targetKey);
        }
    }

    private String slugify(String value) {
        if (value == null) return "item";
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "item" : slug;
    }

    private String lastSegment(String url) {
        if (url == null || url.isBlank()) {
            return "missing";
        }
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private String extOf(String url) {
        String segment = lastSegment(url);
        int dot = segment.lastIndexOf('.');
        if (dot < 0) {
            return ".jpg";
        }
        String ext = segment.substring(dot).toLowerCase(Locale.ROOT);
        return ext.length() > 1 && ext.length() <= 5 ? ext : ".jpg";
    }

    private String contentTypeOf(String url) {
        return switch (extOf(url)) {
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".avif" -> "image/avif";
            case ".gif" -> "image/gif";
            case ".jpg", ".jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    private record MediaResult(boolean uploaded, boolean alreadyPresent, boolean failed, String targetKey) {
        static MediaResult ofUploaded(String key) { return new MediaResult(true, false, false, key); }
        static MediaResult ofAlreadyPresent(String key) { return new MediaResult(false, true, false, key); }
        static MediaResult ofFailed(String key) { return new MediaResult(false, false, true, key); }
        static MediaResult ofSkipped(String key) { return new MediaResult(false, false, false, key); }
        boolean available() { return uploaded || alreadyPresent; }
    }

    private record MigrationOutcome(boolean copied, boolean urlUpdated) {
        static MigrationOutcome none() { return new MigrationOutcome(false, false); }
        static MigrationOutcome urlUpdatedOnly() { return new MigrationOutcome(false, true); }
        static MigrationOutcome of(boolean copied, boolean urlUpdated) { return new MigrationOutcome(copied, urlUpdated); }
    }
}