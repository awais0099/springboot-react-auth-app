package com.project.auth_app_backend.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException() {
        super("Resource not found!");
    }
}
