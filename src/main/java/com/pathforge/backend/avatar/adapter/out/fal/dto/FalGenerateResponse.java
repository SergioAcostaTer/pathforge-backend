package com.pathforge.backend.avatar.adapter.out.fal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FalGenerateResponse(
        List<ImageFile> images,
        String description
) {

    public record ImageFile(
            String url,
            @JsonProperty("content_type") String contentType,
            @JsonProperty("file_name") String fileName,
            @JsonProperty("file_size") Long fileSize,
            Integer width,
            Integer height
    ) {}

    public ImageFile firstImage() {
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("Fal.ai returned no images in response");
        }
        return images.getFirst();
    }
}
