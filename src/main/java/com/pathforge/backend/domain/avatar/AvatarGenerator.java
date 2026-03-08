package com.pathforge.backend.domain.avatar;

/**
 * DOMAIN LAYER — Output port (driven port) for avatar image generation.
 *
 * Defines what the application needs from an external image generation service.
 * The domain declares the contract; the infrastructure adapter fulfils it.
 *
 * Implementations live in adapters/out/fal — never here.
 *
 * Architecture boundary: interface only, no framework annotations, no HTTP logic.
 */
public interface AvatarGenerator {

    /**
     * Generates a stylized avatar image from a source photograph.
     *
     * @param sourceImageUrl publicly accessible URL of the user's photo
     * @param prompt         generation prompt describing the desired avatar style
     * @return               raw image bytes and MIME type of the generated avatar
     */
    GeneratedImage generate(String sourceImageUrl, String prompt);
}
