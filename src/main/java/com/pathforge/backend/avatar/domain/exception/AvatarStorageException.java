package com.pathforge.backend.avatar.domain.exception;

public class AvatarStorageException extends RuntimeException {

    public AvatarStorageException(String message) {
        super(message);
    }

    public AvatarStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
