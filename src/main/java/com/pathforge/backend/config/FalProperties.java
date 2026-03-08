package com.pathforge.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CONFIG LAYER — Strongly-typed binding for the `fal.*` block in application.yml.
 *
 * Registered via @EnableConfigurationProperties in PathForgeApplication.
 * Values can be overridden by environment variables:
 *   FAL_API_KEY, FAL_BASE_URL, FAL_MODEL
 */
@ConfigurationProperties(prefix = "fal")
public record FalProperties(
        String apiKey,
        String baseUrl,
        String model
) {}
