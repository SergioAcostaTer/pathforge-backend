package com.pathforge.backend.adapters.out.fal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ADAPTER LAYER (out/fal) — Deserialised response from the Fal.ai REST API.
 *
 * Fal.ai returns a list of generated images. We take the first result.
 * The "url" field points to a temporary CDN URL from which we download
 * the bytes before uploading to R2.
 */
public record FalGenerateResponse(
        List<FalImage> images,
        Long seed
) {

    public record FalImage(
            String url,
            int width,
            int height,

            @JsonProperty("content_type")
            String contentType
    ) {}

    /**
     * Returns the first generated image URL or throws if the response is empty.
     */
    public FalImage firstImage() {
        if (images == null || images.isEmpty()) {
            throw new IllegalStateException("Fal.ai returned no images in response");
        }
        return images.get(0);
    }
}
