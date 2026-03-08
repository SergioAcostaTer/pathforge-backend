package com.pathforge.backend.avatar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.r2")
public record R2Properties(
        String bucket,
        String endpoint,
        String accessKey,
        String secretKey
) {}
