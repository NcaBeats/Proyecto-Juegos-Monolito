package com.app.proyectojuegosmonolito.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

@Slf4j
@Service
public class R2StorageService {

    private final S3Client s3Client;
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
        this.s3Client = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
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
}