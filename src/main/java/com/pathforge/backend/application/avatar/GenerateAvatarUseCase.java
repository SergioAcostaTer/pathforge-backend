package com.pathforge.backend.application.avatar;

import org.springframework.stereotype.Service;

import com.pathforge.backend.domain.avatar.Avatar;
import com.pathforge.backend.domain.avatar.AvatarGenerator;
import com.pathforge.backend.domain.avatar.AvatarRepository;
import com.pathforge.backend.domain.avatar.GeneratedImage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * APPLICATION LAYER — Use case: Generate Avatar.
 *
 * Orchestrates the business flow without knowing HOW generation or storage work.
 * It depends only on domain ports (interfaces), never on concrete adapters.
 *
 * Flow:
 *   1. Build the generation prompt (business rule — lives here, not in adapters)
 *   2. Delegate image generation to AvatarGenerator port
 *   3. Delegate image storage to AvatarRepository port
 *   4. Return the assembled Avatar domain object
 *
 * The @Service annotation is the only concession to Spring in this layer —
 * it is needed for component scanning and dependency injection wiring.
 * No HTTP, no AWS SDK, no Fal.ai SDK references allowed here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateAvatarUseCase {

    // -----------------------------------------------------------------------
    // Business constant: the avatar generation prompt.
    // Defined here as a domain/application-layer decision, not in adapters.
    // -----------------------------------------------------------------------
    private static final String AVATAR_PROMPT =
            "3D stylized cartoon avatar portrait of a software developer, upper body, " +
            "modern hoodie, studio lighting, centered composition, " +
            "minimal light gray background, high quality illustration, " +
            "professional look, vibrant colors, smooth shading";

    private final AvatarGenerator avatarGenerator; // output port
    private final AvatarRepository avatarRepository; // output port

    /**
     * Executes the avatar generation flow.
     *
     * @param command input data carrying userId and source imageUrl
     * @return        persisted Avatar domain object containing the public URL
     */
    public Avatar execute(GenerateAvatarCommand command) {
        log.info("Starting avatar generation for userId={}", command.userId());

        // Step 1: Generate the stylized avatar image via the generator port.
        // The use case does not know this calls Fal.ai — that is adapter concern.
        GeneratedImage generatedImage = avatarGenerator.generate(command.imageUrl(), AVATAR_PROMPT);

        log.debug("Avatar image generated: {} bytes, type={}",
                generatedImage.data().length, generatedImage.contentType());

        // Step 2: Persist the image via the repository port.
        // The use case does not know this uploads to Cloudflare R2.
        String publicUrl = avatarRepository.store(command.userId(), generatedImage);

        log.info("Avatar stored successfully for userId={}, url={}", command.userId(), publicUrl);

        // Step 3: Construct and return the domain entity.
        return Avatar.create(command.userId(), publicUrl);
    }
}
