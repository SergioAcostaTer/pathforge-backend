package com.pathforge.backend.adapters.in.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pathforge.backend.adapters.in.web.dto.GenerateAvatarRequest;
import com.pathforge.backend.adapters.in.web.dto.GenerateAvatarResponse;
import com.pathforge.backend.application.avatar.GenerateAvatarCommand;
import com.pathforge.backend.application.avatar.GenerateAvatarUseCase;
import com.pathforge.backend.domain.avatar.Avatar;
import com.pathforge.backend.domain.avatar.exception.AvatarGenerationException;
import com.pathforge.backend.domain.avatar.exception.AvatarStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * ADAPTER LAYER (in/web) — Driving adapter that handles HTTP traffic.
 *
 * Responsibilities of this class:
 *   - Deserialise and validate the HTTP request body
 *   - Translate the DTO into a domain command
 *   - Delegate execution to the use case (zero business logic here)
 *   - Map the domain result back to a response DTO
 *   - Handle domain exceptions and return appropriate HTTP status codes
 *
 * Architecture boundary: no business logic, no direct calls to Fal.ai or R2,
 * no domain repository/generator usage. Only the use case may be called.
 */
@Slf4j
@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
public class AvatarController {

    // Depends on the use case (application layer), never on domain ports directly.
    private final GenerateAvatarUseCase generateAvatarUseCase;

    /**
     * POST /api/avatars/generate
     *
     * Accepts a user's source image URL and triggers avatar generation.
     *
     * Example request:
     * {
     *   "user_id": "user-123",
     *   "image_url": "https://example.com/photo.jpg"
     * }
     *
     * Example response (201 Created):
     * {
     *   "avatar_id": "550e8400-e29b-41d4-a716-446655440000",
     *   "user_id": "user-123",
     *   "avatar_url": "https://assets.pathforge.dev/avatars/user-123/avatar.jpg",
     *   "created_at": "2026-03-08T12:00:00Z"
     * }
     */
    @PostMapping("/generate")
    public ResponseEntity<GenerateAvatarResponse> generateAvatar(
            @Valid @RequestBody GenerateAvatarRequest request
    ) {
        log.info("Received avatar generation request for userId={}", request.userId());

        // Translate web DTO → application command (adapter responsibility)
        GenerateAvatarCommand command = new GenerateAvatarCommand(
                request.userId(),
                request.imageUrl()
        );

        // Delegate all business logic to the use case
        Avatar avatar = generateAvatarUseCase.execute(command);

        // Map domain result → response DTO (adapter responsibility)
        GenerateAvatarResponse response = new GenerateAvatarResponse(
                avatar.id(),
                avatar.userId(),
                avatar.publicUrl(),
                avatar.createdAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -----------------------------------------------------------------------
    // Exception handlers — translate domain exceptions into HTTP Problem Details
    // (RFC 7807). These belong in the web adapter, not in the domain.
    // -----------------------------------------------------------------------

    @ExceptionHandler(AvatarGenerationException.class)
    public ResponseEntity<ProblemDetail> handleGenerationException(AvatarGenerationException ex) {
        log.error("Avatar generation failed: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "Avatar generation service is unavailable: " + ex.getMessage());
        problem.setTitle("Avatar Generation Failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(AvatarStorageException.class)
    public ResponseEntity<ProblemDetail> handleStorageException(AvatarStorageException ex) {
        log.error("Avatar storage failed: {}", ex.getMessage(), ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "Avatar storage service is unavailable: " + ex.getMessage());
        problem.setTitle("Avatar Storage Failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }
}
