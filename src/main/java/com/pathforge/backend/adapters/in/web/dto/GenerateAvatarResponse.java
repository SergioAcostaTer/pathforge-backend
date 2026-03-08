package com.pathforge.backend.adapters.in.web.dto;

import java.time.Instant;

/**
 * ADAPTER LAYER (in/web) — Outgoing HTTP response DTO.
 *
 * Maps from the Avatar domain object to a JSON-serialisable record.
 * Domain types must not leak into HTTP responses directly.
 */
public record GenerateAvatarResponse(
        String avatarId,
        String userId,
        String avatarUrl,
        Instant createdAt
) {}
