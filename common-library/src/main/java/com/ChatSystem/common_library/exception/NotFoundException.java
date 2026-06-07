package com.ChatSystem.common_library.exception;

public class NotFoundException extends BusinessException {

    public NotFoundException(String resource, String identifier) {
        super(resource + " not found: " + identifier, 404);
    }
}
