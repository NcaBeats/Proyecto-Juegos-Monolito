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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Set;

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
}
