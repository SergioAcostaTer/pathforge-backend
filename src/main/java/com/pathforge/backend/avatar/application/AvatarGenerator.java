package com.pathforge.backend.avatar.application;

public interface AvatarGenerator {

    ImageData generate(String sourceImageUrl, String prompt);
}
