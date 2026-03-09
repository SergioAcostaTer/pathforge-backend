package com.pathforge.backend.avatar.application.port.in;

import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.domain.AvatarStyle;

public record GenerateAvatarCommand(String userId, ImageData sourceImage, AvatarStyle style) {

    public GenerateAvatarCommand {
        if (userId == null || userId.isBlank())
            throw new IllegalArgumentException("userId must not be blank");
        if (sourceImage == null)
            throw new IllegalArgumentException("sourceImage must not be null");
    }
}
