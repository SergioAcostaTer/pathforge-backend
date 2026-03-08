package com.pathforge.backend.avatar.application;

public interface AvatarRepository {

    /** Stores the user's source photo and returns a presigned URL for Fal.ai to fetch it. */
    String storeSourceImage(String userId, ImageData image);

    /** Stores the generated avatar and returns a 10-minute presigned URL for the client. */
    String storeAvatar(String userId, ImageData image);
}
