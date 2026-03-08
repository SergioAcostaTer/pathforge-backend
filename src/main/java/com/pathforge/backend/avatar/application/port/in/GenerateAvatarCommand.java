package com.pathforge.backend.avatar.application.port.in;

import com.pathforge.backend.avatar.application.ImageData;

public record GenerateAvatarCommand(String userId, ImageData sourceImage) {

    public GenerateAvatarCommand {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId must not be blank");
        if (sourceImage == null)
            throw new IllegalArgumentException("sourceImage must not be null");
    }
}
