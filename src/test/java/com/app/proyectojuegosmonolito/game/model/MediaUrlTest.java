package com.app.proyectojuegosmonolito.game.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * El contrato de media: toda URL que entra al servicio tiene que ser absoluta, y
 * la ruta legacy del trailer se reconstruye sin perder la clave del bucket.
 */
class MediaUrlTest {

    private static final String BASE = "https://pub-test.r2.dev";

    @Test
    void requireAbsolute_whenAbsolute_shouldPassThrough() {
        var url = "https://pub-test.r2.dev/minecraft/trailer.mp4";

        assertThat(MediaUrl.requireAbsolute(url, "videoUrl")).isEqualTo(url);
    }

    @Test
    void requireAbsolute_whenNullOrBlank_shouldAllowIt() {
        // En los endpoints de edicion parcial, null y vacio significan "no cambiar".
        assertThat(MediaUrl.requireAbsolute(null, "videoUrl")).isNull();
        assertThat(MediaUrl.requireAbsolute("", "videoUrl")).isEmpty();
        assertThat(MediaUrl.requireAbsolute("   ", "videoUrl")).isEqualTo("   ");
    }

    @Test
    void requireAbsolute_whenLegacyPath_shouldReject() {
        assertThatThrownBy(() ->
                MediaUrl.requireAbsolute("/uploads/games/minecraft/trailer.mp4", "videoUrl"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("videoUrl");
    }

    @Test
    void requireAbsolute_whenSchemeLessHost_shouldReject() {
        // "cdn.example.com/a.jpg" no es una URL: el navegador la trataria como ruta relativa.
        assertThatThrownBy(() -> MediaUrl.requireAbsolute("cdn.example.com/a.jpg", "imageUrl"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireAbsolute_whenProtocolRelative_shouldReject() {
        assertThatThrownBy(() -> MediaUrl.requireAbsolute("//cdn.example.com/a.jpg", "imageUrl"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toAbsolute_shouldRebuildPublicUrlFromLegacyPath() {
        var result = MediaUrl.toAbsolute("/uploads/games/minecraft/trailer.mp4", BASE);

        assertThat(result).isEqualTo("https://pub-test.r2.dev/minecraft/trailer.mp4");
    }

    @Test
    void toAbsolute_shouldTolerateTrailingSlashInBase() {
        var result = MediaUrl.toAbsolute("/uploads/games/minecraft/trailer.mp4", BASE + "/");

        assertThat(result).isEqualTo("https://pub-test.r2.dev/minecraft/trailer.mp4");
    }

    @Test
    void toAbsolute_whenNotLegacy_shouldReturnUnchanged() {
        var url = "https://otro-cdn.example.com/a.jpg";

        assertThat(MediaUrl.toAbsolute(url, BASE)).isEqualTo(url);
        assertThat(MediaUrl.toAbsolute(null, BASE)).isNull();
    }

    @Test
    void toAbsolute_whenBaseIsBlank_shouldThrowRatherThanProduceBrokenUrl() {
        // Preferimos fallar a guardar "/minecraft/trailer.mp4", que no resuelve a nada.
        assertThatThrownBy(() ->
                MediaUrl.toAbsolute("/uploads/games/minecraft/trailer.mp4", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("public-base-url");
    }

    @Test
    void isLegacyRelative_shouldOnlyMatchTheLegacyPrefix() {
        assertThat(MediaUrl.isLegacyRelative("/uploads/games/minecraft/trailer.mp4")).isTrue();
        assertThat(MediaUrl.isLegacyRelative("/uploads/games/minecraft/card/a.jpg")).isTrue();
        assertThat(MediaUrl.isLegacyRelative("https://pub-test.r2.dev/a.mp4")).isFalse();
        assertThat(MediaUrl.isLegacyRelative("/otra-cosa/a.mp4")).isFalse();
        assertThat(MediaUrl.isLegacyRelative(null)).isFalse();
    }
}