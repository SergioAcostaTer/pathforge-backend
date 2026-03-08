package com.pathforge.backend.domain.avatar.exception;

/**
 * DOMAIN LAYER — Thrown when avatar generation fails at the provider level.
 *
 * Unchecked to avoid polluting call-sites with forced try/catch.
 * Maps to HTTP 502 Bad Gateway in the web adapter.
 */
public class AvatarGenerationException extends RuntimeException {

    public AvatarGenerationException(String message) {
        super(message);
    }

    public AvatarGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
