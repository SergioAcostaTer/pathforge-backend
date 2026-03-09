package com.pathforge.backend.avatar.application.service;

import org.springframework.stereotype.Service;

import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.ImageResizer;
import com.pathforge.backend.avatar.application.port.in.GenerateAvatarCommand;
import com.pathforge.backend.avatar.application.port.out.AvatarGenerator;
import com.pathforge.backend.avatar.application.port.out.AvatarRepository;
import com.pathforge.backend.avatar.application.port.out.BackgroundRemover;
import com.pathforge.backend.avatar.config.AvatarProperties;
import com.pathforge.backend.avatar.domain.Avatar;
import com.pathforge.backend.avatar.domain.AvatarStyle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateAvatarUseCase {

    private final AvatarGenerator avatarGenerator;
    private final AvatarRepository avatarRepository;
    private final AvatarProperties avatarProperties;
    private final ImageResizer imageResizer;
    private final BackgroundRemover backgroundRemover;

    public Avatar execute(GenerateAvatarCommand command) {
        AvatarStyle style = command.style() != null ? command.style() : avatarProperties.defaultStyle();
        log.info("Generating avatar for userId={} style={}", command.userId(), style);

        ImageData normalized = imageResizer.resize(command.sourceImage());
        String sourceImageUrl = avatarRepository.storeSourceImage(command.userId(), normalized);
        ImageData generatedAvatar = avatarGenerator.generate(
                sourceImageUrl, style.prompt(), style.negativePrompt());
        ImageData transparentAvatar = backgroundRemover.removeBackground(generatedAvatar);
        String avatarUrl = avatarRepository.storeAvatar(command.userId(), transparentAvatar);
        return Avatar.create(command.userId(), avatarUrl);
    }
}
