package com.pathforge.backend.avatar.application;

public record GenerateAvatarCommand(String userId, ImageData sourceImage) {

    public GenerateAvatarCommand {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId must not be blank");
        if (sourceImage == null) throw new IllegalArgumentException("sourceImage must not be null");
    }
}
