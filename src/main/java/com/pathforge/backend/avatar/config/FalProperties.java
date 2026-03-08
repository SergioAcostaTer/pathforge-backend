package com.pathforge.backend.avatar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fal")
public record FalProperties(
        String apiKey,
        String baseUrl,
        String model
) {}
