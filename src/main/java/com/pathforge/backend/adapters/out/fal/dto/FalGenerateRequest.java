package com.pathforge.backend.adapters.out.fal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ADAPTER LAYER (out/fal) — Serialised request body sent to the Fal.ai REST API.
 *
 * Fal.ai expects a JSON object with an "input" wrapper for most models.
 * Field names use snake_case as required by the Fal.ai API contract.
 * Jackson annotations override the global snake_case strategy where needed.
 */
public record FalGenerateRequest(
        FalInput input
) {

    /**
     * Inner record representing the model-specific input parameters.
     *
     * For image-to-image / reference-photo models (e.g. fal-ai/flux/dev,
     * fal-ai/pulid, fal-ai/ip-adapter-face-id), "image_url" acts as the
     * reference image. Adjust fields to match the chosen model's schema.
     */
    public record FalInput(
            String prompt,

            @JsonProperty("image_url")
            String imageUrl,

            @JsonProperty("num_inference_steps")
            int numInferenceSteps,

            @JsonProperty("guidance_scale")
            double guidanceScale,

            @JsonProperty("num_images")
            int numImages,

            @JsonProperty("output_format")
            String outputFormat,

            @JsonProperty("enable_safety_checker")
            boolean enableSafetyChecker
    ) {}

    /**
     * Convenience factory with sensible defaults for avatar generation.
     */
    public static FalGenerateRequest of(String prompt, String imageUrl) {
        FalInput input = new FalInput(
                prompt,
                imageUrl,
                28,           // inference steps — quality vs speed tradeoff
                3.5,          // guidance scale
                1,            // generate one image
                "jpeg",       // output format
                true          // run safety checker
        );
        return new FalGenerateRequest(input);
    }
}
