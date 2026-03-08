package com.pathforge.backend.domain.avatar;

import java.time.Instant;

/**
 * DOMAIN LAYER — Avatar aggregate root.
 *
 * Pure Java record with no framework dependencies. Represents a generated avatar
 * stored in object storage. Immutable by design.
 *
 * Architecture boundary: nothing in this class may import Spring, AWS SDK,
 * Jackson, or any other infrastructure library.
 */
public record Avatar(
        String id,
        String userId,
        String publicUrl,
        Instant createdAt
) {

    /**
     * Factory method to create a new Avatar with a generated ID and current timestamp.
     * The ID strategy (UUID, ULID, etc.) is decided here to keep it in the domain.
     */
    public static Avatar create(String userId, String publicUrl) {
        return new Avatar(
                java.util.UUID.randomUUID().toString(),
                userId,
                publicUrl,
                Instant.now()
        );
    }
}
