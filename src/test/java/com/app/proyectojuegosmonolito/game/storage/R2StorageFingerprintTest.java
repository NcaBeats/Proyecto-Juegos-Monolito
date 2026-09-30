package com.app.proyectojuegosmonolito.game.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El fingerprint no es un detalle interno: el navegador lo calcula con
 * {@code crypto.subtle} y lo manda al presign, y el runner de normalizacion lo
 * recalcula desde lo que ya esta en R2. Si las tres implementaciones no coinciden
 * caracter por caracter, la huella que se firmo nunca coincide con la del objeto y
 * la cache immutable queda sirviendo un trailer que no es el que se subio.
 *
 * <p>Estos tests fijan el contrato. El equivalente en TypeScript vive en
 * {@code lib/game-upload.ts}.
 */
class R2StorageFingerprintTest {

    /** 16 MiB: el prefijo que define la huella. */
    private static final long PREFIX = 16L * 1024 * 1024;

    @Test
    @DisplayName("El mismo contenido produce siempre la misma huella")
    void sameContentSameFingerprint() {
        var content = "trailer de prueba".getBytes(StandardCharsets.UTF_8);

        assertEquals(R2StorageService.fingerprint(content), R2StorageService.fingerprint(content));
    }

    @Test
    @DisplayName("Un byte distinto cambia la huella")
    void differentContentDifferentFingerprint() {
        var a = "trailer de prueba A".getBytes(StandardCharsets.UTF_8);
        var b = "trailer de prueba B".getBytes(StandardCharsets.UTF_8);

        assertNotEquals(R2StorageService.fingerprint(a), R2StorageService.fingerprint(b));
    }

    @Test
    @DisplayName("El tamano entra en el digest: el mismo prefijo con otro largo no colisiona")
    void totalSizeIsPartOfTheDigest() {
        var head = "mismos primeros bytes".getBytes(StandardCharsets.UTF_8);

        // Es el escenario del ranged GET del runner: solo lee el prefijo, asi que el
        // tamano total es la unica senal que distingue dos archivos que arrancan igual.
        assertNotEquals(R2StorageService.fingerprint(head, 1_000L),
                R2StorageService.fingerprint(head, 2_000L));
    }

    @Test
    @DisplayName("La huella son 8 hexadecimales en minuscula")
    void fingerprintShape() {
        var fingerprint = R2StorageService.fingerprint("contenido".getBytes(StandardCharsets.UTF_8));

        assertEquals(8, fingerprint.length());
        assertEquals(fingerprint.toLowerCase(java.util.Locale.ROOT), fingerprint);
        assertTrue(fingerprint.matches("[0-9a-f]{8}"), () -> "no es hex de 8 chars: " + fingerprint);
    }

    @Test
    @DisplayName("Solo cuenta el prefijo de 16 MiB, no el archivo entero")
    void onlyThePrefixMatters() {
        // Dos archivos que comparten los primeros 16 MiB y pesan lo mismo: el byte
        // final es la unica diferencia y no debe cambiar la huella, porque el
        // navegador nunca lee mas alla del prefijo.
        var first = buildContent((byte) 0x11);
        var second = buildContent((byte) 0x22);

        assertEquals(PREFIX + 1, first.length);
        assertEquals(R2StorageService.fingerprint(first, first.length),
                R2StorageService.fingerprint(second, second.length));
    }

    @Test
    @DisplayName("Un trailer mas largo con el mismo prefijo si cambia la huella")
    void longerFileWithSamePrefixIsDistinct() {
        var shortFile = buildContent((byte) 0x11);
        var longFile = Arrays.copyOf(shortFile, (int) PREFIX + 1024);

        assertNotEquals(R2StorageService.fingerprint(shortFile, shortFile.length),
                R2StorageService.fingerprint(longFile, longFile.length));
    }

    @Test
    @DisplayName("Con huella la clave queda versionada; sin huella cae en la canonica")
    void trailerKeyShape() {
        assertEquals("gta-v/trailer-a1b2c3d4.mp4", R2StorageService.trailerKey("gta-v", "a1b2c3d4"));
        assertEquals("gta-v/trailer.mp4", R2StorageService.trailerKey("gta-v", null));
        assertEquals("gta-v/trailer.mp4", R2StorageService.trailerKey("gta-v", "  "));
    }

    /** Contenido de PREFIX + 1 bytes con el ultimo byte distinguible. */
    private static byte[] buildContent(byte lastByte) {
        var content = new byte[(int) PREFIX + 1];
        Arrays.fill(content, (byte) 0x7A);
        content[content.length - 1] = lastByte;
        return content;
    }
}
