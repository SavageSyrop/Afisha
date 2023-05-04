package ru.it.lab.exceptions;

public class AuthorizationErrorException extends RuntimeException {
    public AuthorizationErrorException(String message) {
        super("Authorization error occurred: " + message);
    }
}
