package com.pathforge.backend.application.avatar;

/**
 * APPLICATION LAYER — Immutable command object that carries the input data
 * for the GenerateAvatarUseCase.
 *
 * Commands are part of the application layer: they cross the boundary from the
 * driving adapter (web controller) into the use case. They must not contain
 * any framework-specific types so they remain portable across delivery mechanisms.
 *
 * Architecture boundary: no Spring, no Jackson, no validation annotations here.
 * Input validation happens in the web adapter before creating this command.
 */
public record GenerateAvatarCommand(
        String userId,
        String imageUrl
) {

    public GenerateAvatarCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl must not be blank");
        }
    }
}
