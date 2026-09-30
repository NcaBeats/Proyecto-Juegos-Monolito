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
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class R2StorageService {

    private static final Duration PRESIGN_TTL = Duration.ofMinutes(15);

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

    public R2StorageService(
            @Value("${app.r2.account-id}") String accountId,
            @Value("${app.r2.access-key-id}") String accessKeyId,
            @Value("${app.r2.secret-access-key}") String secretAccessKey,
            @Value("${app.r2.bucket}") String bucket,
            @Value("${app.r2.public-base-url}") String publicBaseUrl) {
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
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
        String key = slug + "/trailer.mp4";
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(video.getContentType() != null ? video.getContentType() : "video/mp4")
                        .build(),
                RequestBody.fromBytes(video.getBytes())
        );
        String url = publicBaseUrl + "/" + key;
        log.info("Uploaded video to R2 bucket={} key={} ({} bytes)", bucket, key, video.getSize());
        return url;
    }

    /**
     * Genera una URL firmada para que el navegador suba el trailer directo a R2.
     * El contentType queda firmado: el cliente debe reenviarlo identico en el header,
     * de lo contrario Cloudflare responde 403 SignatureDoesNotMatch.
     */
    public PresignedUploadResponse presignVideo(String slug, String contentType) {
        if (contentType == null || !ALLOWED_VIDEO_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported video content type. Allowed: " + ALLOWED_VIDEO_CONTENT_TYPES);
        }
        String key = slug + "/trailer.mp4";
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGN_TTL)
                        .putObjectRequest(PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .build())
                        .build()
        );
        log.info("Presigned video upload bucket={} key={} contentType={}", bucket, key, contentType);
        return new PresignedUploadResponse(
                presigned.url().toString(),
                key,
                "/uploads/games/" + key,
                contentType,
                PRESIGN_TTL.toSeconds()
        );
    }

    /**
     * Borra el trailer de un juego. Se invoca como limpieza best-effort tras el
     * commit del borrado: si R2 no responde, el juego ya esta eliminado y no
     * debe revertirse por un archivo huerfano.
     *
     * @return {@code true} si el objeto existia y se borro.
     */
    public boolean deleteVideo(String slug) {
        String key = slug + "/trailer.mp4";
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
