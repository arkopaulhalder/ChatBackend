package com.ChatSystem.common_library.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
