package com.pathforge.backend.avatar.adapter.in.web;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pathforge.backend.avatar.adapter.in.web.dto.GenerateAvatarResponse;
import com.pathforge.backend.avatar.application.GenerateAvatarCommand;
import com.pathforge.backend.avatar.application.GenerateAvatarUseCase;
import com.pathforge.backend.avatar.application.ImageData;
import com.pathforge.backend.avatar.domain.Avatar;
import com.pathforge.backend.avatar.domain.exception.AvatarGenerationException;
import com.pathforge.backend.avatar.domain.exception.AvatarStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final GenerateAvatarUseCase generateAvatarUseCase;

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenerateAvatarResponse> generateAvatar(
            @RequestParam("user_id") String userId,
            @RequestPart("image") MultipartFile image
    ) throws IOException {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("user_id is required");
        }
        if (!userId.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("user_id must contain only letters, digits, hyphens or underscores");
        }
        if (image.isEmpty()) {
            throw new IllegalArgumentException("image must not be empty");
        }

        log.info("Avatar generation request for userId={}, file={}, size={}",
                userId, image.getOriginalFilename(), image.getSize());

        String contentType = image.getContentType() != null ? image.getContentType() : "image/jpeg";
        ImageData sourceImage = new ImageData(image.getBytes(), contentType);
        Avatar avatar = generateAvatarUseCase.execute(new GenerateAvatarCommand(userId, sourceImage));

        return ResponseEntity.status(HttpStatus.CREATED).body(new GenerateAvatarResponse(
                avatar.id(),
                avatar.userId(),
                avatar.url(),
                avatar.createdAt()));
    }

    @ExceptionHandler(AvatarGenerationException.class)
    public ResponseEntity<ProblemDetail> handleGenerationException(AvatarGenerationException ex) {
        log.error("Avatar generation failed: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "Avatar generation service unavailable: " + ex.getMessage());
        problem.setTitle("Avatar Generation Failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(AvatarStorageException.class)
    public ResponseEntity<ProblemDetail> handleStorageException(AvatarStorageException ex) {
        log.error("Avatar storage failed: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "Avatar storage service unavailable: " + ex.getMessage());
        problem.setTitle("Avatar Storage Failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
