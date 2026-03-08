package com.pathforge.backend.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * ADAPTER LAYER (in/web) — Incoming HTTP request DTO.
 *
 * Validation annotations live here, not in the domain. The web adapter
 * is responsible for input sanitisation before creating the domain command.
 *
 * Jackson deserialises the JSON body into this record using snake_case
 * (configured globally via spring.jackson.property-naming-strategy=SNAKE_CASE).
 */
public record GenerateAvatarRequest(

        @NotBlank(message = "userId is required")
        String userId,

        @NotBlank(message = "imageUrl is required")
        @Pattern(
            regexp = "^https?://.*",
            message = "imageUrl must be a valid HTTP/HTTPS URL"
        )
        String imageUrl
) {}
