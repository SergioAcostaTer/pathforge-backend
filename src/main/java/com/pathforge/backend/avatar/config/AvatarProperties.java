package com.pathforge.backend.avatar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pathforge.avatar")
public record AvatarProperties(String defaultPrompt, String negativePrompt) {}
