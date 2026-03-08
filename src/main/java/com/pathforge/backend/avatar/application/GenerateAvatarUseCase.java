package com.pathforge.backend.avatar.application;

import org.springframework.stereotype.Service;

import com.pathforge.backend.avatar.domain.Avatar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateAvatarUseCase {

    private static final String AVATAR_PROMPT =
            "3D stylized cartoon avatar portrait of a software developer, upper body, " +
            "modern hoodie, studio lighting, centered composition, " +
            "minimal light gray background, high quality illustration, " +
            "professional look, vibrant colors, smooth shading";

    private final AvatarGenerator avatarGenerator;
    private final AvatarRepository avatarRepository;

    public Avatar execute(GenerateAvatarCommand command) {
        log.info("Starting avatar generation for userId={}", command.userId());

        String sourceImageUrl = avatarRepository.storeSourceImage(command.userId(), command.sourceImage());
        ImageData generatedAvatar = avatarGenerator.generate(sourceImageUrl, AVATAR_PROMPT);
        String avatarUrl = avatarRepository.storeAvatar(command.userId(), generatedAvatar);

        log.info("Avatar generated and stored for userId={}", command.userId());
        return Avatar.create(command.userId(), avatarUrl);
    }
}
