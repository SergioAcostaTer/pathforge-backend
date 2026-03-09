package com.pathforge.backend.avatar.adapter.out.fal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FalGenerateRequest(
        String prompt,
        @JsonProperty("negative_prompt") String negativePrompt,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("image_size") ImageSize imageSize,
        @JsonProperty("num_inference_steps") int numInferenceSteps,
        @JsonProperty("guidance_scale") double guidanceScale,
        double strength,
        @JsonProperty("num_images") int numImages,
        @JsonProperty("output_format") String outputFormat,
        @JsonProperty("enable_safety_checker") boolean enableSafetyChecker
) {

    public record ImageSize(int width, int height) {}

    public static FalGenerateRequest of(String prompt, String negativePrompt, String imageUrl) {
        return new FalGenerateRequest(prompt, negativePrompt, imageUrl, new ImageSize(768, 768), 28, 3.5, 0.65, 1, "png", true);
    }
}
