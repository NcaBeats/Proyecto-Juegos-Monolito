package com.app.proyectojuegosmonolito.game.storage;

import com.app.proyectojuegosmonolito.game.dto.PresignedUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class R2StorageService {

    private static final Duration PRESIGN_TTL = Duration.ofMinutes(15);

    /**
     * Cuantos bytes del principio del archivo entran en la huella. Hashear los 90 MB
     * completos en el navegador cuesta ~6 veces mas (tiempo y memoria) y no aporta
     * nada: dos trailers distintos difieren en los primeros megabytes. El tamano del
     * archivo se suma al digest para que el final del archivo tambien discrimine.
     */
    public static final int FINGERPRINT_BYTES = 16 * 1024 * 1024;

    /**
     * Prefijo de las claves de trailer que ya incorporan huella del contenido.
     */
    public static final String FINGERPRINTED_TRAILER_PREFIX = "trailer-";

    private static final String UPLOADS_PREFIX = "/uploads/games/";

    private static final Set<String> ALLOWED_VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime",
            "video/x-m4v"
    );

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/avif",
            "image/gif"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final String publicBaseUrl;
    private final String imageCacheControl;
    private final String videoCacheControl;

    public R2StorageService(
            @Value("${app.r2.account-id}") String accountId,
            @Value("${app.r2.access-key-id}") String accessKeyId,
            @Value("${app.r2.secret-access-key}") String secretAccessKey,
            @Value("${app.r2.bucket}") String bucket,
            @Value("${app.r2.public-base-url}") String publicBaseUrl,
            @Value("${app.r2.cache-control-image}") String imageCacheControl,
            @Value("${app.r2.cache-control-video}") String videoCacheControl) {
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
        this.imageCacheControl = imageCacheControl;
        this.videoCacheControl = videoCacheControl;
        var endpoint = URI.create("https://" + accountId + ".r2.cloudflarestorage.com");
        var credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        this.s3Client = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(endpoint)
                .credentialsProvider(credentials)
                .build();
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of("auto"))
                .endpointOverride(endpoint)
                .credentialsProvider(credentials)
                .build();
    }

    public String storeVideo(MultipartFile video, String slug) throws IOException {
        var bytes = video.getBytes();
        var key = trailerKey(slug, fingerprint(bytes));
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(video.getContentType() != null ? video.getContentType() : "video/mp4")
                        .cacheControl(videoCacheControl)
                        .build(),
                RequestBody.fromBytes(bytes)
        );
        log.info("Uploaded video to R2 bucket={} key={} ({} bytes)", bucket, key, video.getSize());
        return key;
    }

    /**
     * Huella corta del contenido, derivada de los primeros
     * {@link #FINGERPRINT_BYTES} bytes mas el tamano total. Es lo que hace que la
     * clave del trailer cambie sola cuando cambia el archivo, en vez de depender
     * de un contador que alguien tenga que acordarse de incrementar.
     */
    public static String fingerprint(byte[] content) {
        return fingerprint(content, content.length);
    }

    static String fingerprint(byte[] head, long totalSize) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.update(head, 0, Math.min(head.length, FINGERPRINT_BYTES));
            digest.update(Long.toString(totalSize).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest(), 0, 4);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en la JVM", e);
        }
    }

    /**
     * Clave del trailer para un contenido dado. Sin huella devuelve la clave
     * canonica {@code {slug}/trailer.mp4}, que es la que usaban las subidas
     * anteriores a este mecanismo.
     */
    public static String trailerKey(String slug, String fingerprint) {
        return fingerprint == null || fingerprint.isBlank()
                ? slug + "/trailer.mp4"
                : slug + "/" + FINGERPRINTED_TRAILER_PREFIX + fingerprint + ".mp4";
    }

    /**
     * Deriva la clave dentro del bucket desde cualquiera de las dos formas en que
     * se persisten las URLs: absoluta ({@code https://...r2.dev/{key}}) o relativa
     * ({@code /uploads/games/{key}}). Descarta el query string, que existe solo
     * para el cache del navegador y no forma parte de la clave.
     */
    public String keyOf(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String u = url.trim();
        int query = u.indexOf('?');
        if (query >= 0) {
            u = u.substring(0, query);
        }
        if (u.contains("res.cloudinary.com")) {
            return null;
        }
        if (u.startsWith(UPLOADS_PREFIX)) {
            return u.substring(UPLOADS_PREFIX.length());
        }
        int scheme = u.indexOf("://");
        if (scheme >= 0) {
            String rest = u.substring(scheme + 3);
            int slash = rest.indexOf('/');
            return slash < 0 ? null : rest.substring(slash + 1);
        }
        return u.contains("/") ? u : null;
    }

    /**
     * Genera una URL firmada para que el navegador suba el trailer directo a R2.
     * El contentType y el cacheControl quedan firmados: el cliente debe reenviarlos
     * identicos en el PUT, de lo contrario Cloudflare responde 403
     * SignatureDoesNotMatch. El cacheControl vuelve en la respuesta justamente para
     * que el cliente no tenga su propia copia de esa constante: si las dos valores
     * divergen, toda subida falla en silencio.
     *
     * @param fingerprint huella del contenido calculada por el cliente; si viene
     *                    vacia se usa la clave canonica {slug}/trailer.mp4
     */
    public PresignedUploadResponse presignVideo(String slug, String contentType, String fingerprint) {
        if (contentType == null || !ALLOWED_VIDEO_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported video content type. Allowed: " + ALLOWED_VIDEO_CONTENT_TYPES);
        }
        String key = trailerKey(slug, fingerprint);
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGN_TTL)
                        .putObjectRequest(PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .cacheControl(videoCacheControl)
                                .build())
                        .build()
        );
        log.info("Presigned video upload bucket={} key={} contentType={}", bucket, key, contentType);
        return new PresignedUploadResponse(
                presigned.url().toString(),
                key,
                UPLOADS_PREFIX + key,
                contentType,
                videoCacheControl,
                PRESIGN_TTL.toSeconds()
        );
    }

    /**
     * Borra el trailer de un juego a partir de su clave, que ya incluye la huella
     * del contenido si la subida vino de este mecanismo. Se invoca como limpieza
     * best-effort tras el commit: si R2 no responde, el juego ya quedo con la URL
     * nueva y no debe revertirse por un archivo huerfano.
     *
     * @return {@code true} si el objeto existia y se borro.
     */
    public boolean deleteTrailer(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build());
            log.info("Deleted video from R2 bucket={} key={}", bucket, key);
            return true;
        } catch (SdkException e) {
            log.warn("Could not delete video from R2 bucket={} key={}: {}", bucket, key, e.getMessage());
            return false;
        }
    }

    /**
     * Genera una URL firmada para que el navegador suba la imagen directo a R2.
     * La clave queda fijada por el backend: {slug}/{kind}/{uuid}.{ext}, donde kind
     * es card, banner o gallery. El contentType queda firmado: el cliente debe
     * reenviarlo identico en el header, de lo contrario devuelve 403 SignatureDoesNotMatch.
     */
    public PresignedUploadResponse presignImage(String slug, String kind, String contentType) {
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported image content type. Allowed: " + ALLOWED_IMAGE_CONTENT_TYPES);
        }
        String key = slug + "/" + kind + "/" + UUID.randomUUID() + extFor(contentType);
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGN_TTL)
                        .putObjectRequest(PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .cacheControl(imageCacheControl)
                                .build())
                        .build()
        );
        String publicPath = publicBaseUrl + "/" + key;
        log.info("Presigned image upload bucket={} key={} contentType={}", bucket, key, contentType);
        return new PresignedUploadResponse(
                presigned.url().toString(),
                key,
                publicPath,
                contentType,
                imageCacheControl,
                PRESIGN_TTL.toSeconds()
        );
    }

    /**
     * Sube una imagen servida por el backend directamente a R2.
     */
    public String storeImage(MultipartFile file, String slug, String kind) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported image content type. Allowed: " + ALLOWED_IMAGE_CONTENT_TYPES);
        }
        String key = slug + "/" + kind + "/" + UUID.randomUUID() + extFor(contentType);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .cacheControl(imageCacheControl)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
        String url = publicBaseUrl + "/" + key;
        log.info("Uploaded image to R2 bucket={} key={} ({} bytes)", bucket, key, file.getSize());
        return url;
    }

    /**
     * Sube bytes ya obtenidos (usado por el runner de migracion Cloudinary -&gt; R2).
     */
    public String storeImageBytes(String key, byte[] bytes, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .cacheControl(imageCacheControl)
                        .build(),
                RequestBody.fromBytes(bytes)
        );
        String url = publicBaseUrl + "/" + key;
        log.info("Uploaded image bytes to R2 bucket={} key={} ({} bytes)", bucket, key, bytes.length);
        return url;
    }

    /**
     * URL publica de una key dentro del bucket.
     */
    public String publicUrl(String key) {
        return publicBaseUrl + "/" + key;
    }

    /**
     * Ruta relativa que se persiste en la entidad y que el frontend resuelve contra
     * el host publico del bucket. Es la contraparte de {@link #publicUrl(String)}.
     */
    public String publicPath(String key) {
        return UPLOADS_PREFIX + key;
    }

    /**
     * Normaliza el Cache-Control de un objeto ya subido. No mueve bytes: copia el
     * objeto sobre si mismo reemplazando la metadata, que es la unica via para
     * editar headers de un objeto existente en R2. El contentType se reenvia desde
     * el HEAD porque MetadataDirective.REPLACE descarta todo lo que no se declare.
     *
     * @return {@code true} si el objeto fue modificado; {@code false} si ya tenia
     *         la politica esperada o si Cloudflare no respondio.
     */
    public boolean applyCacheControl(String key) {
        var head = head(key);
        if (head == null) {
            return false;
        }
        var desired = cacheControlFor(key);
        if (desired.equals(head.cacheControl())) {
            return false;
        }
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(key)
                    .destinationBucket(bucket)
                    .destinationKey(key)
                    .metadataDirective(MetadataDirective.REPLACE)
                    .contentType(head.contentType())
                    .cacheControl(desired)
                    .build());
            log.info("Applied Cache-Control to bucket={} key={} value={}", bucket, key, desired);
            return true;
        } catch (SdkException e) {
            log.warn("Could not apply Cache-Control to {}: {}", bucket + "/" + key, e.getMessage());
            return false;
        }
    }

    /**
     * Todas las claves del bucket, paginadas. La lista no distingue objetos ya
     * borrados de los que nunca existieron: ambos aparecen con status 404 en el HEAD.
     */
    public List<String> listKeys() {
        var keys = new ArrayList<String>();
        String token = null;
        do {
            var response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .continuationToken(token)
                    .build());
            response.contents().forEach(object -> keys.add(object.key()));
            token = response.isTruncated() ? response.nextContinuationToken() : null;
        } while (token != null);
        return keys;
    }

    /**
     * Huella del contenido de un objeto que ya esta en el bucket, calculada sobre
     * los primeros {@link #FINGERPRINT_BYTES} bytes mediante un GET con Range. Solo
     * transfiere ese prefijo, no el archivo completo, que para un trailer de 90 MB
     * seria la diferencia entre 16 MB y 90 MB por objeto.
     *
     * @return la huella, o {@code null} si el objeto no existe o no se pudo leer.
     */
    public String fingerprintOf(String key) {
        var head = head(key);
        if (head == null) {
            return null;
        }
        long totalSize = head.contentLength() == null ? 0 : head.contentLength();
        try (var object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .range("bytes=0-" + (FINGERPRINT_BYTES - 1))
                .build())) {
            return fingerprint(object.readAllBytes(), totalSize);
        } catch (IOException | SdkException e) {
            log.warn("Could not read prefix of {}: {}", bucket + "/" + key, e.getMessage());
            return null;
        }
    }

    /**
     * El Cache-Control que corresponde a la clave segun su tipo. Los trailers y las
     * imagenes comparten politica porque ambas familias de claves son unicas por
     * version (huella del contenido y UUID respectivamente), y esa unicidad es
     * justamente lo que hace seguro declarar immutable.
     */
    private String cacheControlFor(String key) {
        var lower = key.toLowerCase(java.util.Locale.ROOT);
        boolean isVideo = lower.endsWith(".mp4") || lower.endsWith(".webm")
                || lower.endsWith(".mov") || lower.endsWith(".m4v");
        return isVideo ? videoCacheControl : imageCacheControl;
    }

    private HeadObjectResponse head(String key) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (SdkException e) {
            log.warn("Could not read metadata of {}: {}", bucket + "/" + key, e.getMessage());
            return null;
        }
    }

    /**
     * Copia server-side un objeto dentro del bucket (S3 CopyObject). Usado por el
     * runner de consolidacion para unificar los trailers bajo el slug canonical del
     * juego sin mover bytes por el servidor.
     *
     * @return {@code true} si la copia se completo; {@code false} si el origen no
     *         existe o Cloudflare no respondio.
     */
    public boolean copyObject(String sourceKey, String destKey) {
        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucket)
                    .destinationKey(destKey)
                    .build());
            log.info("Copied R2 object bucket={} {} -> {}", bucket, sourceKey, destKey);
            return true;
        } catch (SdkException e) {
            log.warn("Could not copy R2 object bucket={} {} -> {}: {}", bucket, sourceKey, destKey, e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si un objeto ya existe en el bucket (usado por el runner para saltar
     * objetos ya migrados). Ante un error no concluyente devuelve {@code false}.
     */
    public boolean imageExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (SdkException e) {
            log.warn("Could not check existence of R2 bucket={} key={}: {}", bucket, key, e.getMessage());
            return false;
        }
    }

    /**
     * Borra una imagen de R2 a partir de su URL. Las URLs que no pertenecen a este
     * bucket (p.ej. Cloudinary legacy durante la transicion) se ignoran.
     *
     * @return {@code true} si el objeto pertenecia al bucket y existia.
     */
    public boolean deleteImage(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        if (!url.startsWith(publicBaseUrl)) {
            log.info("Skipping deletion of non-R2 image: {}", url);
            return false;
        }
        String key = url.substring(publicBaseUrl.length() + 1);
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build());
            log.info("Deleted image from R2 bucket={} key={}", bucket, key);
            return true;
        } catch (SdkException e) {
            log.warn("Could not delete image from R2 bucket={} key={}: {}", bucket, key, e.getMessage());
            return false;
        }
    }

    private String extFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/avif" -> ".avif";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
