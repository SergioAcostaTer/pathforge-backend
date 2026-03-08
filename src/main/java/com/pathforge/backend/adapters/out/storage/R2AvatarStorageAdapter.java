package com.pathforge.backend.adapters.out.storage;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pathforge.backend.config.R2Properties;
import com.pathforge.backend.domain.avatar.AvatarRepository;
import com.pathforge.backend.domain.avatar.GeneratedImage;
import com.pathforge.backend.domain.avatar.exception.AvatarStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * ADAPTER LAYER (out/storage) — Implements the AvatarRepository domain port using
 * Cloudflare R2 (AWS S3-compatible object storage).
 *
 * This class is the ONLY place in the application that knows about Cloudflare R2
 * or the AWS SDK. It handles:
 *   - Deriving a deterministic storage key from userId
 *   - Uploading image bytes via the AWS S3 SDK v2
 *   - Constructing and returning the public CDN URL
 *   - Mapping SDK exceptions to domain exceptions
 *
 * Architecture boundary:
 *   - Implements domain port: AvatarRepository
 *   - No business logic allowed here — only infrastructure concern
 *   - Domain layer has zero knowledge of S3, buckets, or object keys
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class R2AvatarStorageAdapter implements AvatarRepository {

    private final S3Client r2Client;
    private final R2Properties r2Properties;

    /**
     * {@inheritDoc}
     *
     * Storage key format:  avatars/{userId}/{timestamp}-{uuid}.jpg
     * This scheme allows:
     *   - Multiple avatars per user (historical record)
     *   - Cache-busting via unique key per generation
     *   - Organised prefix for lifecycle policies
     */
    @Override
    public String store(String userId, GeneratedImage image) {
        String objectKey = buildObjectKey(userId, image.contentType());
        log.debug("Uploading avatar to R2: bucket={} key={}", r2Properties.bucket(), objectKey);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(r2Properties.bucket())
                    .key(objectKey)
                    .contentType(image.contentType())
                    .contentLength((long) image.data().length)
                    // R2 does not support ACL — objects are public via custom domain
                    .build();

            r2Client.putObject(putRequest, RequestBody.fromBytes(image.data()));

            String publicUrl = buildPublicUrl(objectKey);
            log.info("Avatar uploaded successfully: {}", publicUrl);
            return publicUrl;

        } catch (S3Exception e) {
            log.error("R2 upload failed for userId={}: code={} message={}",
                    userId, e.awsErrorDetails().errorCode(), e.getMessage(), e);
            throw new AvatarStorageException(
                    "Failed to upload avatar to storage: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during R2 upload for userId={}", userId, e);
            throw new AvatarStorageException("Unexpected error during avatar upload", e);
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers — infrastructure detail, not domain logic
    // -----------------------------------------------------------------------

    private String buildObjectKey(String userId, String contentType) {
        String extension = contentTypeToExtension(contentType);
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("avatars/%s/%s-%s.%s", userId, timestamp, uniqueId, extension);
    }

    private String buildPublicUrl(String objectKey) {
        String baseUrl = r2Properties.publicUrl().endsWith("/")
                ? r2Properties.publicUrl()
                : r2Properties.publicUrl() + "/";
        return baseUrl + objectKey;
    }

    private String contentTypeToExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg"; // jpeg / image/jpeg / unknown → .jpg
        };
    }
}
