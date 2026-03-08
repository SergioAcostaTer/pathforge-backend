package com.pathforge.backend.avatar.adapter.in.web.dto;

import java.time.Instant;

public record GenerateAvatarResponse(
        String avatarId,
        String userId,
        String avatarUrl,
        Instant createdAt
) {}
