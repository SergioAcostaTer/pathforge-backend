package com.pathforge.backend.avatar.adapter.out.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.port.out.AvatarRepository;
import com.pathforge.backend.avatar.config.R2Properties;
import com.pathforge.backend.avatar.domain.exception.AvatarStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class R2AvatarStorageAdapter implements AvatarRepository {

    private static final Duration SOURCE_IMAGE_TTL = Duration.ofMinutes(15);
    private static final Duration AVATAR_TTL = Duration.ofMinutes(10);

    private final S3Client r2Client;
    private final S3Presigner r2Presigner;
    private final R2Properties r2Properties;

    @Override
    public String storeSourceImage(String userId, ImageData image) {
        return upload("uploads/" + userId, image, SOURCE_IMAGE_TTL);
    }

    @Override
    public String storeAvatar(String userId, ImageData image) {
        return upload("avatars/" + userId, image, AVATAR_TTL);
    }

    private String upload(String keyPrefix, ImageData image, Duration presignTtl) {
        String objectKey = buildObjectKey(keyPrefix, image.contentType());
        log.debug("Uploading to R2: bucket={} key={}", r2Properties.bucket(), objectKey);

        try {
            r2Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(r2Properties.bucket())
                            .key(objectKey)
                            .contentType(image.contentType())
                            .contentLength((long) image.data().length)
                            .build(),
                    RequestBody.fromBytes(image.data()));

            return presign(objectKey, presignTtl);

        } catch (S3Exception e) {
            log.error("R2 upload failed key={}: code={}", objectKey, e.awsErrorDetails().errorCode(), e);
            throw new AvatarStorageException("Failed to upload to storage: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during R2 upload key={}", objectKey, e);
            throw new AvatarStorageException("Unexpected error during upload", e);
        }
    }

    private String presign(String objectKey, Duration ttl) {
        PresignedGetObjectRequest presigned = r2Presigner.presignGetObject(r -> r
                .signatureDuration(ttl)
                .getObjectRequest(get -> get
                        .bucket(r2Properties.bucket())
                        .key(objectKey)));
        return presigned.url().toString();
    }

    private String buildObjectKey(String prefix, String contentType) {
        String ext = switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
        return String.format("%s/%s-%s.%s",
                prefix, Instant.now().toEpochMilli(), UUID.randomUUID().toString().substring(0, 8), ext);
    }
}
