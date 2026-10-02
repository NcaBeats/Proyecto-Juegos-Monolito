package com.app.proyectojuegosmonolito.game.model;

import java.util.regex.Pattern;

/**
 * Contrato unico de las URLs de media de {@link Game}: todas son absolutas.
 *
 * <p>Existieron dos convenciones conviviendo. El seed y el flujo de imagen
 * guardaban URLs publicas de R2, pero el trailer pasaba por
 * {@code "/uploads/games/{slug}/trailer.mp4"}: una ruta que el backend se
 * inventaba y que el frontend tenia que reconstruir contra R2. Que dos caminos
 * de escritura produjeran formas distintas es la raiz del problema, no la
 * divergencia de datos.
 *
 * <p>Por eso esta clase existe: para que el servicio no pueda persistir una ruta
 * relativa, sin importar por que camino entre. El valor null o vacio sigue
 * siendo valido y significa "no cambiar"; lo que se rechaza es una ruta.
 */
public final class MediaUrl {

    /**
     * Prefijo que usaba el flujo multipart para el trailer. Solo aparece en
     * filas escritas antes de que el servicio dejara de emitir rutas relativas;
     * sirve para que el runner de normalizacion las identifique.
     */
    public static final String LEGACY_TRAILER_PREFIX = "/uploads/games/";

    /**
     * Se admite http porque en desarrollo las imagenes pueden servirse desde el
     * backend local; en produccion R2 siempre es https.
     */
    private static final Pattern ABSOLUTE = Pattern.compile("^https?://\\S+$");

    private MediaUrl() {
    }

    public static boolean isAbsolute(String url) {
        return url != null && ABSOLUTE.matcher(url).matches();
    }

    /**
     * Exige que la URL sea utilizable tal cual por el frontend. Null o vacio
     * pasan de largo: significan "dejar el valor anterior intacto".
     *
     * @throws IllegalArgumentException si la URL no es absoluta (mapea a 400)
     */
    public static String requireAbsolute(String url, String field) {
        if (url == null || url.isBlank()) {
            return url;
        }
        if (!isAbsolute(url)) {
            throw new IllegalArgumentException(
                    field + " debe ser una URL absoluta http/https, no una ruta. Recibido: " + url);
        }
        return url;
    }

    /** True si la URL es una ruta legacy del trailer y por lo tanto hay que normalizarla. */
    public static boolean isLegacyRelative(String url) {
        return url != null && url.startsWith(LEGACY_TRAILER_PREFIX);
    }

    /**
     * Reconstruye la URL publica a partir de una ruta legacy del trailer.
     * La clave dentro del bucket es exactamente lo que va despues del prefijo:
     * {@code /uploads/games/minecraft/trailer.mp4} -> {@code {base}/minecraft/trailer.mp4}.
     *
     * @return la URL absoluta, o el mismo valor de entrada si no era una ruta legacy
     */
    public static String toAbsolute(String url, String publicBaseUrl) {
        if (!isLegacyRelative(url)) {
            return url;
        }
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "No se puede normalizar '" + url + "' porque app.r2.public-base-url esta vacio");
        }
        String base = publicBaseUrl.replaceAll("/+$", "");
        return base + "/" + url.substring(LEGACY_TRAILER_PREFIX.length());
    }
}