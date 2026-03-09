package com.pathforge.backend.avatar.adapter.out.fal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FalGenerateRequest(
        String prompt,
        @JsonProperty("image_urls") List<String> imageUrls,
        @JsonProperty("num_images") int numImages,
        @JsonProperty("aspect_ratio") String aspectRatio,
        @JsonProperty("output_format") String outputFormat,
        @JsonProperty("safety_tolerance") String safetyTolerance
) {

    public static FalGenerateRequest of(String prompt, String imageUrl) {
        return new FalGenerateRequest(prompt, List.of(imageUrl), 1, "1:1", "png", "4");
    }
}
