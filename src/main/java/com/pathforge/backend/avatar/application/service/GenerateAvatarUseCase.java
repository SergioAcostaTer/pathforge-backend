package com.pathforge.backend.avatar.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.application.port.in.GenerateAvatarCommand;
import com.pathforge.backend.avatar.domain.Avatar;

import lombok.extern.slf4j.Slf4j;
import main.java.com.pathforge.backend.avatar.application.port.out.AvatarGenerator;
import main.java.com.pathforge.backend.avatar.application.port.out.AvatarRepository;

@Slf4j
@Service
public class GenerateAvatarUseCase {

    private final AvatarGenerator avatarGenerator;
    private final AvatarRepository avatarRepository;
    private final String defaultPrompt;

    public GenerateAvatarUseCase(
            AvatarGenerator avatarGenerator,
            AvatarRepository avatarRepository,
            @Value("${pathforge.avatar.default-prompt}") String defaultPrompt) {
        this.avatarGenerator = avatarGenerator;
        this.avatarRepository = avatarRepository;
        this.defaultPrompt = defaultPrompt;
    }

    public Avatar execute(GenerateAvatarCommand command) {
        log.info("Starting avatar generation for userId={}", command.userId());

        String sourceImageUrl = avatarRepository.storeSourceImage(command.userId(), command.sourceImage());
        ImageData generatedAvatar = avatarGenerator.generate(sourceImageUrl, defaultPrompt);
        String avatarUrl = avatarRepository.storeAvatar(command.userId(), generatedAvatar);

        log.info("Avatar generated and stored for userId={}", command.userId());
        return Avatar.create(command.userId(), avatarUrl);
    }
}
