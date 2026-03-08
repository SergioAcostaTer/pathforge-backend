package com.pathforge.backend.avatar.domain;

import java.time.Instant;
import java.util.UUID;

public record Avatar(
        String id,
        String userId,
        String url,
        Instant createdAt
) {

    public static Avatar create(String userId, String url) {
        return new Avatar(UUID.randomUUID().toString(), userId, url, Instant.now());
    }
}
