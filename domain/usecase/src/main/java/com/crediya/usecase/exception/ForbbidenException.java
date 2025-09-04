package com.crediya.usecase.exception;

public class ForbbidenException extends IllegalArgumentException{
    public ForbbidenException(String message) {
        super(message);
    }
}
