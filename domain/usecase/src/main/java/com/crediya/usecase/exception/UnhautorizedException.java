package com.crediya.usecase.exception;

public class UnhautorizedException extends IllegalArgumentException{
    public UnhautorizedException(String message) {
        super(message);
    }
}
