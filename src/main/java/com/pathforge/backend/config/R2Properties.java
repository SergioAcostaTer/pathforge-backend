package com.pathforge.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CONFIG LAYER — Strongly-typed binding for the `storage.r2.*` block in application.yml.
 *
 * Registered via @EnableConfigurationProperties in PathForgeApplication.
 * Values can be overridden by environment variables:
 *   R2_BUCKET, R2_ENDPOINT, R2_ACCESS_KEY, R2_SECRET_KEY, R2_PUBLIC_URL
 */
@ConfigurationProperties(prefix = "storage.r2")
public record R2Properties(
        String bucket,
        String endpoint,
        String accessKey,
        String secretKey,
        String publicUrl
) {}
