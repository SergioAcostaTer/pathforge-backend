package com.pathforge.backend.avatar.domain.exception;

public class AvatarGenerationException extends RuntimeException {

    public AvatarGenerationException(String message) {
        super(message);
    }

    public AvatarGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
