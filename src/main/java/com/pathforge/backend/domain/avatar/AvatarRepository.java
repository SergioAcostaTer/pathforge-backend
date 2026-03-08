package com.pathforge.backend.domain.avatar;

/**
 * DOMAIN LAYER — Output port (driven port) for avatar persistence.
 *
 * Defines what the application needs from a storage backend.
 * The domain only knows it can store an image and get back a public URL — it has
 * no knowledge of S3, R2, buckets, or presigned URLs.
 *
 * Implementations live in adapters/out/storage — never here.
 *
 * Architecture boundary: interface only, no framework annotations, no AWS SDK.
 */
public interface AvatarRepository {

    /**
     * Persists the generated image bytes and returns the public URL where
     * the stored avatar can be accessed.
     *
     * @param userId       owner of the avatar (used to derive the storage key)
     * @param image        generated image data and content type
     * @return             publicly accessible URL of the stored avatar
     */
    String store(String userId, GeneratedImage image);
}
