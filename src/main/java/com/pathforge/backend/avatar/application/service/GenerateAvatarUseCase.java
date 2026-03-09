package com.pathforge.backend.avatar.application.service;

import org.springframework.stereotype.Service;

import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.ImageResizer;
import com.pathforge.backend.avatar.application.port.in.GenerateAvatarCommand;
import com.pathforge.backend.avatar.application.port.out.AvatarGenerator;
import com.pathforge.backend.avatar.application.port.out.AvatarRepository;
import com.pathforge.backend.avatar.config.AvatarProperties;
import com.pathforge.backend.avatar.domain.Avatar;

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

    public Avatar execute(GenerateAvatarCommand command) {
        ImageData normalized = imageResizer.resize(command.sourceImage());
        String sourceImageUrl = avatarRepository.storeSourceImage(command.userId(), normalized);
        ImageData generatedAvatar = avatarGenerator.generate(
                sourceImageUrl, avatarProperties.defaultPrompt(), avatarProperties.negativePrompt());
        String avatarUrl = avatarRepository.storeAvatar(command.userId(), generatedAvatar);
        return Avatar.create(command.userId(), avatarUrl);
    }
}
