package com.app.proyectojuegosmonolito.game.service;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * La firma de Cloudinary se recalcula aqui de forma independiente del codigo de
 * produccion. Si la implementacion de {@link ImageService#signUpload} cambia el
 * orden de los parametros, deja de incluirlos o alters el algoritmo, estos tests
 * fallan en vez de devolver un 401 en produccion que nadie puede depurar.
 */
class ImageServiceSignUploadTest {

    private static final String CLOUD_NAME = "test-cloud";
    private static final String API_KEY = "000000000000000";
    private static final String API_SECRET = "testonlyapisecrettestonlyapisecret";
    private static final String CLOUDINARY_URL =
            "cloudinary://" + API_KEY + ":" + API_SECRET + "@" + CLOUD_NAME;

    private ImageService service() {
        return new ImageService(CLOUDINARY_URL);
    }

    @Test
    void signUpload_shouldReturnCloudinaryEndpoint() {
        var result = service().signUpload("games/prueba/card");

        assertThat(result.uploadUrl())
                .isEqualTo("https://api.cloudinary.com/v1_1/test-cloud/image/upload");
        assertThat(result.cloudName()).isEqualTo(CLOUD_NAME);
        assertThat(result.apiKey()).isEqualTo(API_KEY);
        assertThat(result.folder()).isEqualTo("games/prueba/card");
    }

    @Test
    void signUpload_shouldProduceSignatureMatchingTheDocumentedAlgorithm() throws Exception {
        var result = service().signUpload("games/prueba/banner");

        // Algoritmo de Cloudinary: params no vacios ordenados por clave, unidos con &,
        // como key=value, con el api_secret pegado al final, y SHA-1 en hex.
        var payload = "folder=" + result.folder() + "&timestamp=" + result.timestamp() + API_SECRET;
        var expected = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-1").digest(payload.getBytes(StandardCharsets.UTF_8)));

        assertThat(result.signature()).isEqualTo(expected);
    }

    @Test
    void signUpload_shouldProduce40HexCharacters() {
        var result = service().signUpload("games/prueba/gallery");

        assertThat(result.signature()).hasSize(40).matches("[0-9a-f]{40}");
    }

    @Test
    void signUpload_shouldChangeSignatureWhenFolderChanges() {
        var card = service().signUpload("games/prueba/card");
        var banner = service().signUpload("games/prueba/banner");

        // Mismo timestamp casi seguro, asi que la unica diferencia es folder.
        assertThat(card.signature()).isNotEqualTo(banner.signature());
    }

    @Test
    void signUpload_shouldReturnTimestampCloseToNow() {
        long before = System.currentTimeMillis() / 1000;
        var result = service().signUpload("games/prueba/card");
        long after = System.currentTimeMillis() / 1000;

        assertThat(result.timestamp()).isBetween(before, after);
    }

    @Test
    void signUpload_whenCloudinaryIsNotConfigured_shouldThrow() {
        var sinConfig = new ImageService("cloudinary://000000000000000:@test-cloud");

        assertThatThrownBy(() -> sinConfig.signUpload("games/prueba/card"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CLOUDINARY_URL");
    }
}
