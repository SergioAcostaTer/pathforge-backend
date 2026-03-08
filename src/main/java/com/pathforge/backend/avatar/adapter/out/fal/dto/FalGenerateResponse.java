package com.pathforge.backend.avatar.adapter.out.fal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FalGenerateResponse(
        List<FalImage> images,
        Long seed
) {

    public record FalImage(
            String url,
            int width,
            int height,
            @JsonProperty("content_type") String contentType
    ) {}

    public FalImage firstImage() {
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("Fal.ai returned no images in response");
        }
        return images.getFirst();
    }
}
