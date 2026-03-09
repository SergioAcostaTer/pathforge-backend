package com.pathforge.backend.avatar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.pathforge.backend.avatar.domain.AvatarStyle;

@ConfigurationProperties(prefix = "pathforge.avatar")
public record AvatarProperties(AvatarStyle defaultStyle) {}
