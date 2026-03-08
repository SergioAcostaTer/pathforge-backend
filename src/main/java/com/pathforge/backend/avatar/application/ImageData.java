package com.pathforge.backend.avatar.application;

public record ImageData(byte[] data, String contentType) {

    public ImageData {
        if (data == null || data.length == 0) throw new IllegalArgumentException("Image data must not be empty");
        if (contentType == null || contentType.isBlank()) throw new IllegalArgumentException("Content type must not be blank");
    }
}
