package com.pathforge.backend.domain.avatar.exception;

/**
 * DOMAIN LAYER — Thrown when avatar persistence to object storage fails.
 *
 * Maps to HTTP 502 Bad Gateway in the web adapter.
 */
public class AvatarStorageException extends RuntimeException {

    public AvatarStorageException(String message) {
        super(message);
    }

    public AvatarStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
