package com.pathforge.backend.avatar.application.port.out;

import com.pathforge.backend.avatar.application.ImageData;

public interface AvatarGenerator {

    ImageData generate(String sourceImageUrl, String prompt);
}
