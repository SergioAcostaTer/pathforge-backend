package com.pathforge.backend.avatar.adapter.out.fal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FalGenerateRequest(
        String prompt,
        @JsonProperty("image_url") String imageUrl,
        @JsonProperty("num_inference_steps") int numInferenceSteps,
        @JsonProperty("guidance_scale") double guidanceScale,
        @JsonProperty("num_images") int numImages,
        @JsonProperty("output_format") String outputFormat,
        @JsonProperty("enable_safety_checker") boolean enableSafetyChecker
) {

    public static FalGenerateRequest of(String prompt, String imageUrl) {
        return new FalGenerateRequest(prompt, imageUrl, 28, 3.5, 1, "jpeg", true);
    }
}
