package com.pathforge.backend.domain.avatar;

/**
 * DOMAIN LAYER — Value object representing raw image data returned by an avatar generator.
 *
 * Carries the binary content and its MIME type so the storage adapter
 * can set the correct Content-Type header on upload.
 *
 * Architecture boundary: no framework or infrastructure imports allowed.
 */
public record GeneratedImage(
        byte[] data,
        String contentType
) {

    public GeneratedImage {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Generated image data must not be empty");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("Content type must not be blank");
        }
    }
}
