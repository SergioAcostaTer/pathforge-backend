package com.pathforge.backend.avatar.adapter.out.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.port.out.AvatarRepository;
import com.pathforge.backend.avatar.config.R2Properties;
import com.pathforge.backend.avatar.domain.exception.AvatarStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Component
@RequiredArgsConstructor
public class R2AvatarStorageAdapter implements AvatarRepository {

    private static final Duration SOURCE_IMAGE_TTL = Duration.ofMinutes(15);
    private static final Duration AVATAR_TTL = Duration.ofMinutes(10);

    private final S3Client r2Client;
    private final S3Presigner r2Presigner;
    private final R2Properties r2Properties;

    @PostConstruct
    void ensureBucketExists() {
        String bucket = r2Properties.bucket();
        try {
            r2Client.headBucket(b -> b.bucket(bucket));
            log.info("R2 bucket '{}' is ready", bucket);
        } catch (NoSuchBucketException e) {
            log.info("R2 bucket '{}' not found — creating it", bucket);
            r2Client.createBucket(b -> b.bucket(bucket));
            log.info("R2 bucket '{}' created", bucket);
        } catch (S3Exception e) {
            log.warn("Could not verify R2 bucket '{}': {}", bucket, e.awsErrorDetails().errorMessage());
        }
    }

    @Override
    public String storeSourceImage(String userId, ImageData image) {
        return upload("uploads/" + userId, image, SOURCE_IMAGE_TTL);
    }

    @Override
    public String storeAvatar(String userId, ImageData image) {
        return upload("avatars/" + userId, image, AVATAR_TTL);
    }

    private String upload(String keyPrefix, ImageData image, Duration ttl) {
        String ext = switch (image.contentType()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
        String key = keyPrefix + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        log.debug("Uploading to R2: bucket={} key={}", r2Properties.bucket(), key);

        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(r2Properties.bucket())
                            .key(key)
                            .contentType(image.contentType())
                            .contentLength((long) image.data().length)
                            .build(),
                    RequestBody.fromBytes(image.data()));

            return r2Presigner.presignGetObject(r -> r
                    .signatureDuration(ttl)
                    .getObjectRequest(get -> get.bucket(r2Properties.bucket()).key(key)))
                    .url().toString();

        } catch (S3Exception e) {
            log.error("R2 upload failed key={}: {}", key, e.awsErrorDetails().errorMessage(), e);
            throw new AvatarStorageException("Failed to upload to storage: " + e.awsErrorDetails().errorMessage(), e);
        }
    }
}
